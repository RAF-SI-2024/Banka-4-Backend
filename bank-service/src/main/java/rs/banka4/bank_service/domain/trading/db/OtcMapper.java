package rs.banka4.bank_service.domain.trading.db;

import org.mapstruct.*;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.security.stock.db.Stock;
import rs.banka4.bank_service.domain.security.stock.mapper.StockMapper;
import rs.banka4.bank_service.domain.trading.db.dtos.OtcRequestCreateDto;
import rs.banka4.bank_service.domain.trading.db.dtos.OtcRequestDto;
import rs.banka4.bank_service.domain.trading.db.dtos.OtcRequestUpdateDto;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        StockMapper.class
    },
    componentModel = "spring"
)
public interface OtcMapper {
    @Mapping(
        target = "madeBy",
        source = "madeBy"
    )
    @Mapping(
        target = "madeFor",
        source = "madeFor"
    )
    @Mapping(
        target = "modifiedBy",
        source = "modifiedBy"
    )
    @Mapping(
        target = "amount",
        source = "otcRequest.amount"
    )
    @Mapping(
        target = "latestStockPrice",
        source = "latestPrice"
    )
    OtcRequestDto toOtcRequestDto(
        OtcRequest otcRequest,
        String madeBy,
        String madeFor,
        String modifiedBy,
        MonetaryAmount latestPrice
    );

    @Mapping(
        target = "stock",
        source = "stock"
    )
    @Mapping(
        target = "id",
        ignore = true
    )
    OtcRequest toOtcRequest(
        OtcRequestCreateDto otcRequestCreateDto,
        ForeignBankId madeBy,
        ForeignBankId madeFor,
        ForeignBankId modifiedBy,
        RequestStatus status,
        Stock stock
    );

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget OtcRequest otc, OtcRequestUpdateDto dto, ForeignBankId modifiedBy);
}
