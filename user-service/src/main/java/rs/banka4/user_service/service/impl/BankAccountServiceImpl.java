package rs.banka4.user_service.service.impl;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.company.db.Company;
import rs.banka4.user_service.domain.transaction.db.Transaction;
import rs.banka4.user_service.domain.transaction.dtos.TransactionDto;
import rs.banka4.user_service.domain.transaction.mapper.TransactionMapper;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.exceptions.account.AccountNotFound;
import rs.banka4.user_service.exceptions.account.NotAccountOwner;
import rs.banka4.user_service.exceptions.company.CompanyNotFound;
import rs.banka4.user_service.exceptions.user.UserNotFound;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.CompanyRepository;
import rs.banka4.user_service.repositories.TransactionRepository;
import rs.banka4.user_service.service.abstraction.BankAccountService;
import rs.banka4.user_service.service.abstraction.JwtService;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private static final String BANK_COMPANY_NAME = "Raffeisen Bank";

    private final CompanyRepository companyRepository;
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final JwtService jwtService;

    public List<Account> getBankAccounts() {
        Company bank =
            companyRepository.findByName(BANK_COMPANY_NAME)
                .orElseThrow(() -> new CompanyNotFound(BANK_COMPANY_NAME));

        return accountRepository.findAllByCompany(bank);
    }

    public Account getBankAccountForCurrency(CurrencyCode.Code currency) {
        return getBankAccounts().stream()
            .filter(
                account -> account.getCurrency()
                    .getCode()
                    == currency
            )
            .findFirst()
            .orElseThrow(
                () -> new AccountNotFound("Bank account for currency " + currency + " not found")
            );
    }

    public Page<TransactionDto> getAllTransactionsForBank(
        Authentication authentication,
        PageRequest pageRequest
    ) {
        UUID clientId =
            jwtService.extractUserId(
                authentication.getCredentials()
                    .toString()
            );
        Client client =
            clientRepository.findById(clientId)
                .orElseThrow(() -> new UserNotFound(clientId.toString()));

        Company bank =
            companyRepository.findByName(BANK_COMPANY_NAME)
                .orElseThrow(() -> new CompanyNotFound(BANK_COMPANY_NAME));

        if (
            !bank.getMajorityOwner()
                .equals(client)
        ) {
            throw new NotAccountOwner();
        }

        Page<Transaction> transactions = transactionRepository.findAllByCompany(bank, pageRequest);

        return transactions.map(TransactionMapper.INSTANCE::toDto);
    }

    public Client getBankOwner() {
        return getBankAccountForCurrency(CurrencyCode.Code.RSD).getCompany()
            .getMajorityOwner();
    }
}
