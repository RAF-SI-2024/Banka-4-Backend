package rs.banka4.bank_service.domain.card.db;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CardType {
    DEBIT("Debit"),
    CREDIT("Credit");

    private final String type;
}
