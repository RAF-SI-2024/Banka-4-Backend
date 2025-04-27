package rs.banka4.bank_service.domain.actuaries.db.dto;

import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;

public record ActuaryProfitDto(
    MonetaryAmount profit,
    String name,
    String surname,
    String position
) {
}
