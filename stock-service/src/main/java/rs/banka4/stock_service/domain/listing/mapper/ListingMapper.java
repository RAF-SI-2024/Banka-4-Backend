package rs.banka4.stock_service.domain.listing.mapper;

import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import rs.banka4.stock_service.domain.listing.db.Listing;
import rs.banka4.stock_service.domain.listing.dtos.ListingDto;
import rs.banka4.stock_service.domain.listing.dtos.ListingInfoDto;
import rs.banka4.stock_service.domain.listing.dtos.specific.ForexPairDto;
import rs.banka4.stock_service.domain.listing.dtos.specific.FutureDto;
import rs.banka4.stock_service.domain.listing.dtos.specific.StockDto;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.future.db.Future;
import rs.banka4.stock_service.domain.security.stock.db.Stock;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ListingMapper {

    ListingMapper INSTANCE = Mappers.getMapper(ListingMapper.class);

    Listing toEntity(ListingDto dto);

    ListingDto toDto(Listing listing);

    StockDto toStockDto(Stock stock);

    ForexPairDto toForexPairDto(ForexPair forexPair);

    FutureDto toFutureDto(Future future);

    @Mapping(
        target = "securityId",
        expression = "java(listing.getSecurity().getId())"
    )
    @Mapping(
        target = "price",
        source = "listing.ask"
    )
    @Mapping(
        target = "name",
        expression = "java(listing.getSecurity().getName())"
    )
    @Mapping(
        target = "ticker",
        expression = "java(listing.getSecurity().getTicker())"
    )
    @Mapping(
        target = "volume",
        source = "vol"
    )
    @Mapping(
        target = "change",
        source = "change"
    )
    ListingInfoDto toInfoDto(Listing listing, int vol, BigDecimal change);
}
