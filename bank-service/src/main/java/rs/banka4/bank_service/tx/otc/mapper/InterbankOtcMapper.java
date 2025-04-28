package rs.banka4.bank_service.tx.otc.mapper;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import rs.banka4.bank_service.domain.trading.db.OtcRequest;
import rs.banka4.bank_service.tx.data.OtcNegotiation;
import rs.banka4.bank_service.tx.data.OtcOffer;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface InterbankOtcMapper {
    InterbankOtcMapper INSTANCE = Mappers.getMapper(InterbankOtcMapper.class);

    @Mapping(
        source = "pricePerUnit",
        target = "pricePerStock"
    )
    @Mapping(
        target = "madeBy",
        source = "buyerId"
    )
    @Mapping(
        target = "madeFor",
        source = "sellerId"
    )
    @Mapping(
        target = "modifiedBy",
        source = "lastModifiedBy"
    )
    @Mapping(
        target = "stock",
        ignore = true
    )
    @Mapping(
        target = "settlementDate",
        expression = "java(otcOffer.settlementDate().toLocalDate())"
    )
    OtcRequest toOtcRequest(OtcOffer otcOffer);

    @Mapping(
        source = "otcRequest.pricePerStock",
        target = "pricePerUnit"
    )
    @Mapping(
        source = "otcRequest.madeBy",
        target = "buyerId"
    )
    @Mapping(
        source = "otcRequest.madeFor",
        target = "sellerId"
    )
    @Mapping(
        source = "otcRequest.modifiedBy",
        target = "lastModifiedBy"
    )
    @Mapping(
        target = "stock",
        expression = "java(new StockDescription(otcRequest.getStock().getTicker()))"
    )
    @Mapping(
        target = "settlementDate",
        expression = "java(InterbankOtcMapper.midnightSettlementDate(otcRequest))"
    )
    OtcNegotiation toOtcNegotiation(OtcRequest otcRequest, boolean isOngoing);

    @Mapping(
        source = "pricePerStock",
        target = "pricePerUnit"
    )
    @Mapping(
        source = "madeBy",
        target = "buyerId"
    )
    @Mapping(
        source = "madeFor",
        target = "sellerId"
    )
    @Mapping(
        source = "modifiedBy",
        target = "lastModifiedBy"
    )
    @Mapping(
        target = "stock",
        expression = "java(new StockDescription(otcRequest.getStock().getTicker()))"
    )
    @Mapping(
        target = "settlementDate",
        expression = "java(InterbankOtcMapper.midnightSettlementDate(otcRequest))"
    )
    OtcOffer toOtcOffer(OtcRequest otcRequest);

    static OffsetDateTime midnightSettlementDate(OtcRequest otcRequest) {
        return OffsetDateTime.of(otcRequest.getSettlementDate(), LocalTime.of(0, 0), ZoneOffset.UTC)
            .plusDays(1);
    }
}
