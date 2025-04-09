package rs.banka4.stock_service.domain.listing.dtos.specific;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.stock_service.domain.listing.dtos.ListingDetailsDto;
import rs.banka4.stock_service.domain.security.forex.db.ForexLiquidity;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Dto for additional fields for ForexPair security")
public class ForexPairDto extends ListingDetailsDto {
    private CurrencyCode baseCurrency;
    private CurrencyCode quoteCurrency;
    private ForexLiquidity liquidity;
    private BigDecimal exchangeRate;
}
