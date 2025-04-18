package rs.banka4.bank_service.service.impl;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rs.banka4.bank_service.domain.account.db.Account;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.options.db.Option;
import rs.banka4.bank_service.domain.orders.db.Direction;
import rs.banka4.bank_service.domain.orders.db.Order;
import rs.banka4.bank_service.domain.security.stock.db.Stock;
import rs.banka4.bank_service.domain.taxes.db.UserTaxDebts;
import rs.banka4.bank_service.domain.taxes.db.dto.TaxableUserDto;
import rs.banka4.bank_service.exceptions.transaction.InsufficientFunds;
import rs.banka4.bank_service.repositories.AccountRepository;
import rs.banka4.bank_service.repositories.UserTaxDebtsRepository;
import rs.banka4.bank_service.runners.TestDataRunner;
import rs.banka4.bank_service.service.abstraction.ProfitCalculationService;
import rs.banka4.bank_service.service.abstraction.TaxCalculationService;
import rs.banka4.bank_service.service.abstraction.TaxService;

@Service
@RequiredArgsConstructor
public class TaxServiceImp implements TaxService {
    private final UserTaxDebtsRepository userTaxDebtsRepository;
    private final TaxCalculationService taxCalculationService;
    private final AccountRepository accountRepository;
    private final ProfitCalculationService profitCalculationService;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TaxServiceImp.class);

    @Override
    public Page<TaxableUserDto> getTaxSummary(String firstName, String lastName, PageRequest of) {
        return userTaxDebtsRepository.findClientsWithDebt(firstName, lastName, of)
            .map(user -> {
                var debts = userTaxDebtsRepository.findByAccount_Client_Id(user.getId());
                var debtTotal = taxCalculationService.calculateTax(debts);
                return new TaxableUserDto(
                    user.getId(),
                    user.getFirstName(),
                    user.getEmail(),
                    user.getLastName(),
                    debtTotal.unpaidTaxThisMonth(),
                    debtTotal.currency()
                );
            });
    }

    @Override
    public void taxUser(UUID userId) {
        var debts = userTaxDebtsRepository.findByAccount_Client_Id(userId);
        var stateAcc =
            accountRepository.findAccountByAccountNumber(TestDataRunner.STATE_ACCOUNT_NUMBER)
                .orElseThrow(() -> new IllegalArgumentException("State account not found"));
        debts.forEach(debt -> taxCalculationService.chargeTax(debt.getAccount(), stateAcc));
    }

    @Scheduled(cron = "0 0 0 1 * *")
    @Override
    public void taxMonthly() {
        var stateAcc =
            accountRepository.findAccountByAccountNumber(TestDataRunner.STATE_ACCOUNT_NUMBER)
                .orElseThrow(() -> new IllegalArgumentException("State account not found"));
        userTaxDebtsRepository.findAll()
            .forEach(debt -> {
                try {
                    taxCalculationService.chargeTax(debt.getAccount(), stateAcc);
                } catch (InsufficientFunds e) {
                    logger.error("Insufficient funds in account " + debt.getAccount());
                }
            });
    }

    @Transactional
    @Override
    public void addTaxForOrderToDB(Order order) {
        if (
            !order.getDirection()
                .equals(Direction.SELL)
        ) return;
        if (!(order.getAsset() instanceof Stock)) return;
        var realizedProfitForSellOrder =
            profitCalculationService.calculateRealizedProfitForSell(order);
        addTaxAmountToDB(realizedProfitForSellOrder, order.getAccount());
    }

    @Transactional
    @Override
    public void addTaxForOtcToDB(Option option, Account account) {
        var profit = profitCalculationService.calculateOptionProfit(option);
        addTaxAmountToDB(profit, account);
    }

    @Transactional
    @Override
    public void addTaxAmountToDB(MonetaryAmount profitAmount, Account account) {
        if (
            !profitAmount.getCurrency()
                .equals(account.getCurrency())
        ) throw new IllegalArgumentException("Currency mismatch between account and tax amount");
        if (
            profitAmount.getAmount()
                .compareTo(BigDecimal.ZERO)
                <= 0
        ) return;

        var debt =
            userTaxDebtsRepository.findByAccount_AccountNumber(account.getAccountNumber())
                .orElseGet(() -> {
                    var d = new UserTaxDebts();
                    d.setAccount(account);
                    d.setDebtAmount(BigDecimal.ZERO);
                    d.setYearlyDebtAmount(BigDecimal.ZERO);
                    return d;
                });
        var taxAmt =
            profitAmount.getAmount()
                .multiply(new BigDecimal("0.15"));
        var newDebtAmt =
            debt.getDebtAmount()
                .add(taxAmt);
        debt.setDebtAmount(newDebtAmt);

        userTaxDebtsRepository.save(debt);
    }

    @Scheduled(cron = "0 0 0 1 1 *")
    public void cleanYearlyDebt() {
        userTaxDebtsRepository.findAll()
            .forEach(debt -> {
                debt.setYearlyDebtAmount(BigDecimal.ZERO);
                userTaxDebtsRepository.save(debt);
            });
    }
}
