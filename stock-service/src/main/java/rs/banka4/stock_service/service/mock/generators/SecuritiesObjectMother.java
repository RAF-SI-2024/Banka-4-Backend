package rs.banka4.stock_service.service.mock.generators;

import rs.banka4.stock_service.domain.security.SecurityDto;
import rs.banka4.stock_service.domain.security.forex.dtos.ForexPairDto;
import rs.banka4.stock_service.domain.security.future.dtos.FutureDto;
import rs.banka4.stock_service.domain.security.stock.dtos.StockDto;

public class SecuritiesObjectMother {
    public static SecurityDto generateStockSecurityDto() {
        StockDto stockDto = StockObjectMother.generateStockDto();
        return new SecurityDto(
            stockDto.id(),
            stockDto.name(),
            stockDto.price(),
            stockDto,
            null,
            null
        );
    }

    public static SecurityDto generateFutureSecurityDto() {
        FutureDto futureDto = FutureObjectMother.generateFutureDto();
        return new SecurityDto(
            futureDto.id(),
            futureDto.name(),
            futureDto.price(),
            null,
            futureDto,
            null
        );
    }

    public static SecurityDto generateForexPairSecurityDto() {
        ForexPairDto forexPairDto = ForexPairObjectMother.generateForexPairDto();
        String name = forexPairDto.baseCurrency() + "/" + forexPairDto.quoteCurrency();
        return new SecurityDto(
            forexPairDto.id(),
            name,
            forexPairDto.price(),
            null,
            null,
            forexPairDto
        );
    }
}
