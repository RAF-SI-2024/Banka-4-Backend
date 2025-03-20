package rs.banka4.stock_service.domain.security.stock.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import rs.banka4.stock_service.domain.security.stock.db.Stock;
import rs.banka4.stock_service.domain.security.stock.dtos.StockDto;

@Mapper
public interface StockMapper {

    StockMapper INSTANCE = Mappers.getMapper(StockMapper.class);

    Stock toEntity(StockDto dto);

    StockDto toDto(Stock stock);
}
