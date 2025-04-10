package rs.banka4.stock_service.generator;

import java.math.BigDecimal;
import java.util.UUID;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.stock_service.domain.actuaries.db.ActuaryInfo;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;

public class ActuaryObjectMother {

    /**
     * Generates a basic ActuaryInfo instance.
     *
     * @return a new ActuaryInfo instance
     */
    public static ActuaryInfo generateBasicActuaryInfo() {
        return ActuaryInfo.builder()
            .userId(UUID.fromString("987e6543-e21b-45d3-b456-426614174111"))
            .needApproval(true)
            .limit(new MonetaryAmount(BigDecimal.valueOf(1_000_000), CurrencyCode.RSD))
            .usedLimit(new MonetaryAmount(BigDecimal.valueOf(1000), CurrencyCode.RSD))
            .build();
    }

}
