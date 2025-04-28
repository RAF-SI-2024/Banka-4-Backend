package rs.banka4.bank_service.service.abstraction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import rs.banka4.bank_service.domain.account.db.Account;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.transaction.db.TransactionStatus;
import rs.banka4.bank_service.domain.transaction.dtos.CreatePaymentDto;
import rs.banka4.bank_service.domain.transaction.dtos.CreateTransferDto;
import rs.banka4.bank_service.domain.transaction.dtos.TransactionDto;
import rs.banka4.rafeisen.common.currency.CurrencyCode;

public interface TransactionService {
    TransactionDto createTransaction(
        Authentication authentication,
        CreatePaymentDto createPaymentDto
    );

    TransactionDto createTransfer(
        Authentication authentication,
        CreateTransferDto createTransferDto
    );

    Page<TransactionDto> getAllTransactionsForClient(
        String token,
        TransactionStatus paymentStatus,
        BigDecimal amount,
        LocalDate paymentDate,
        String accountNumber,
        PageRequest pageRequest
    );

    TransactionDto getTransactionById(String token, UUID transactionId);

    Page<TransactionDto> getAllTransfersForClient(String token, PageRequest pageRequest);

    void createOrderTransaction(
        CreatePaymentDto createPaymentDto,
        CurrencyCode toCurrency,
        ForeignBankId id
    );

    void createFeeTransaction(
        Account fromAccount,
        String toAccountNumber,
        CurrencyCode toCurrency,
        BigDecimal fee,
        ForeignBankId id
    );
}
