package rs.banka4.user_service.domain.card.db;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CardName {
    VISA("Visa"),
    MASTER_CARD("MasterCard"),
    DINA_CARD("DinaCard"),
    AMERICAN_EXPRESS("American Express");

    private final String name;
}
