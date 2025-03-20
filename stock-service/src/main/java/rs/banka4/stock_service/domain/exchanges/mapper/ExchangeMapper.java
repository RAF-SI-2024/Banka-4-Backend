package rs.banka4.stock_service.domain.exchanges.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;
import rs.banka4.stock_service.domain.exchanges.dtos.ExchangeDto;

@Mapper
public interface ExchangeMapper {

    ExchangeMapper INSTANCE = Mappers.getMapper(ExchangeMapper.class);

    Exchange toEntity(ExchangeDto dto);

    ExchangeDto toDto(Exchange exchange);
}
