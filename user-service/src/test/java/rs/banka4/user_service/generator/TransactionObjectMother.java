package rs.banka4.user_service.generator;

import rs.banka4.user_service.domain.transaction.dtos.CreatePaymentDto;
import rs.banka4.user_service.domain.transaction.dtos.CreateTransactionDto;
import rs.banka4.user_service.domain.transaction.dtos.TransactionDto;
import rs.banka4.user_service.domain.transaction.db.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionObjectMother {

    public static CreateTransactionDto generateBasicCreateTransactionDto() {
        return new CreateTransactionDto(
                "102-39443942389",
                "102-394438340549",
                BigDecimal.valueOf(1.00),
                "EUR",
                "Milutin Joncic",
                "289",
                "1176926",
                "Temu payment"
        );
    }

    public static TransactionDto generateBasicTransactionDto() {
        return new TransactionDto(
                UUID.randomUUID(),
                "1265463698391",
                "444394438340549",
                "444394438340549",
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
                LocalDateTime.now(),
                TransactionStatus.REALIZED
        );
    }

    public static CreatePaymentDto generateBasicCreatePaymentDto() {
        return new CreatePaymentDto(
                "102-39443942389",
                "102-394438340549",
                BigDecimal.valueOf(1.00),
                "Milutin Joncic",
                "289",
                "1176926",
                "Temu payment"
        );
    }
}