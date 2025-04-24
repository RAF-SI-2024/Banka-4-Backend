package rs.banka4.bank_service.service.impl;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.banka4.bank_service.domain.account.db.Account;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.taxes.db.UserTaxDebts;
import rs.banka4.bank_service.domain.taxes.db.dto.UserTaxInfoDto;
import rs.banka4.bank_service.domain.transaction.db.Transaction;
import rs.banka4.bank_service.domain.transaction.db.TransactionStatus;
import rs.banka4.bank_service.exceptions.transaction.InsufficientFunds;
import rs.banka4.bank_service.repositories.AccountRepository;
import rs.banka4.bank_service.repositories.TransactionRepository;
import rs.banka4.bank_service.repositories.UserTaxDebtsRepository;
import rs.banka4.bank_service.service.abstraction.ExchangeRateService;
import rs.banka4.bank_service.service.abstraction.TaxCalculationService;
import rs.banka4.rafeisen.common.currency.CurrencyCode;

@Service
@RequiredArgsConstructor
public class TaxCalculationServiceImpl implements TaxCalculationService {
    private final TransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;
    private final UserTaxDebtsRepository userTaxDebtsRepository;
    private final AccountRepository accountRepository;

    public UserTaxInfoDto calculateTax(List<UserTaxDebts> debts) {
        var totalUnpaid =
            debts.stream()
                .map(debt -> {
                    CurrencyCode accountCurrency =
                        debt.getAccount()
                            .getCurrency();
                    if (!accountCurrency.equals(CurrencyCode.RSD)) {
                        return exchangeRateService.convertCurrency(
                            debt.getDebtAmount(),
                            accountCurrency,
                            CurrencyCode.RSD
                        );
                    }
                    return debt.getDebtAmount();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var totalYearly =
            debts.stream()
                .map(UserTaxDebts::getYearlyDebtAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new UserTaxInfoDto(totalYearly, totalUnpaid, CurrencyCode.RSD);
    }

    @Transactional
    public void chargeTax(Account accountFrom, Account accountTo) {
        /// This will never throw because we call this function for acc from UserTaxDebt table
        UserTaxDebts debt =
            userTaxDebtsRepository.findByAccount_AccountNumber(accountFrom.getAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Account has no tax debt"));

        var rawDebt = debt.getDebtAmount();
        var amountToDebit = rawDebt;

        if (
            accountFrom.getAvailableBalance()
                .compareTo(rawDebt)
                < 0
        ) {
            throw new InsufficientFunds();
        }

        if (
            !accountFrom.getCurrency()
                .equals(accountTo.getCurrency())
        ) {
            amountToDebit =
                exchangeRateService.convertCurrency(
                    rawDebt,
                    accountFrom.getCurrency(),
                    accountTo.getCurrency()
                );
        }

        accountFrom.setAvailableBalance(
            accountFrom.getAvailableBalance()
                .subtract(rawDebt)
        );
        accountFrom.setBalance(
            accountFrom.getBalance()
                .subtract(rawDebt)
        );

        accountTo.setAvailableBalance(
            accountTo.getAvailableBalance()
                .add(amountToDebit)
        );
        accountTo.setBalance(
            accountTo.getBalance()
                .add(amountToDebit)
        );

        debt.setDebtAmount(BigDecimal.ZERO);
        debt.setYearlyDebtAmount(
            debt.getYearlyDebtAmount()
                .add(amountToDebit)
        );
        userTaxDebtsRepository.save(debt);

        accountRepository.saveAll(List.of(accountFrom, accountTo));
        transactionRepository.save(buildTransfer(accountFrom, accountTo, rawDebt, amountToDebit));
    }

    /**
     * Builds a transfer transaction between two accounts.
     *
     * @param fromAccount the source account
     * @param toAccount the destination account
     * @param fromAmount the amount debited from the source (in source currency)
     * @param toAmount the amount credited to the destination (in destination currency)
     * @return a new {@link Transaction} entity ready for persistence
     */
    private Transaction buildTransfer(
        Account fromAccount,
        Account toAccount,
        BigDecimal fromAmount,
        BigDecimal toAmount

    ) {
        return Transaction.builder()
            .transactionNumber(
                UUID.randomUUID()
                    .toString()
            )
            .fromAccount(fromAccount)
            .toAccount(toAccount)
            .from(new MonetaryAmount(fromAmount, fromAccount.getCurrency()))
            .to(new MonetaryAmount(toAmount, toAccount.getCurrency()))
            .recipient(toAccount.getClient().firstName)
            .paymentCode("101")
            .fee(new MonetaryAmount(BigDecimal.ZERO, toAccount.getCurrency()))
            .referenceNumber(
                String.valueOf(
                    toAccount.getClient()
                        .getId()
                )
            )
            .paymentPurpose("Internal")
            .paymentDateTime(LocalDateTime.now())
            .status(TransactionStatus.REALIZED)
            .build();
    }
}
