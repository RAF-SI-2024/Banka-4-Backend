package rs.banka4.user_service.generator;

import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.transaction.db.MonetaryAmount;
import rs.banka4.user_service.domain.transaction.db.Transaction;
import rs.banka4.user_service.domain.transaction.dtos.CreatePaymentDto;
import rs.banka4.user_service.domain.transaction.dtos.CreateTransferDto;
import rs.banka4.user_service.domain.transaction.dtos.TransactionDto;
import rs.banka4.user_service.domain.transaction.db.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionObjectMother {

    public static TransactionDto generateBasicTransactionDto() {
        return new TransactionDto(
                UUID.fromString("8b14aa1d-0633-44d3-a74a-e699b35909d2"),
                "1265463698391",
                "444394438340549",
                "444394438340523",
                BigDecimal.valueOf(1.00),
                "EUR",
                BigDecimal.valueOf(1.00),
                "RSD",
                BigDecimal.valueOf(0.10),
                "EUR",
                "Milutin Joncic",
                "289",
                "1176926",
                "Temu payment",
                LocalDateTime.of(2021, 3, 21, 23, 59, 59),
                TransactionStatus.REALIZED
        );
    }

    public static CreatePaymentDto generateBasicCreatePaymentDto() {
        return new CreatePaymentDto(
                "444394438340549",
                "444394438340523",
                BigDecimal.valueOf(1.00),
                "Milutin Joncic",
                true,
                "289",
                "1176926",
                "Temu payment",
                "123123"
        );
    }

    public static CreateTransferDto generateBasicCreateTransferDto() {
        return new CreateTransferDto(
    "444394438340549",
                "444394438340523",
                BigDecimal.valueOf(1.0),
                "123123"
        )  ;
    }

    public static Transaction generateBasicTransaction(Account fromAccount, Account toAccount) {
        return Transaction.builder()
                .id(UUID.fromString("8b14aa1d-0633-44d3-a74a-e699b35909d2"))
                .transactionNumber("1265463698391")
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .from(new MonetaryAmount(BigDecimal.valueOf(1.00), generateCurrency("EUR")))
                .to(new MonetaryAmount(BigDecimal.valueOf(1.00), generateCurrency("RSD")))
                .fee(new MonetaryAmount(BigDecimal.valueOf(0.10), generateCurrency("EUR")))
                .recipient("Milutin Joncic")
                .paymentCode("289")
                .referenceNumber("1176926")
                .paymentPurpose("Temu payment")
                .paymentDateTime(LocalDateTime.of(2021, 3, 21, 23, 59, 59))
                .status(TransactionStatus.REALIZED)
                .build();
    }

    public static Currency generateCurrency(String code) {
        return Currency.builder()
                .id(UUID.randomUUID())
                .name("Fake")
                .symbol("X")
                .description("Fake currency")
                .active(true)
                .code(Currency.Code.fromString(code))
                .build();
    }

    public static TransactionDto generateTransactionDto(UUID id, String fromAccount, String toAccount, BigDecimal amount, String currency, TransactionStatus status) {
        return new TransactionDto(
                id,
                "1265463698391",
                fromAccount,
                toAccount,
                amount,
                currency,
                amount,
                currency,
                BigDecimal.valueOf(0.10),
                currency,
                "Recipient",
                "289",
                "1176926",
                "Payment purpose",
                LocalDateTime.now(),
                status
        );
    }

    public static Transaction generateTransaction(UUID id, Account fromAccount, Account toAccount, BigDecimal amount, Currency currency, TransactionStatus status) {
        return Transaction.builder()
                .id(id)
                .transactionNumber(id.toString())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .from(new MonetaryAmount(amount, currency))
                .to(new MonetaryAmount(amount, currency))
                .fee(new MonetaryAmount(BigDecimal.valueOf(0.10), currency))
                .recipient("Recipient")
                .paymentCode("289")
                .referenceNumber("1176926")
                .paymentPurpose("Payment purpose")
                .paymentDateTime(LocalDateTime.of(2021, 3, 21, 23, 59, 59))
                .status(status)
                .build();
    }
}