package rs.banka4.bank_service.tx.otc.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import rs.banka4.bank_service.domain.trading.db.OtcRequest;
import rs.banka4.bank_service.tx.data.OtcOffer;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
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
    OtcRequest toOtcRequest(OtcOffer otcOffer);
}
