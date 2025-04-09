package rs.banka4.stock_service.domain.orders.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import rs.banka4.stock_service.domain.orders.db.Order;
import rs.banka4.stock_service.domain.orders.dtos.CreateOrderDto;
import rs.banka4.stock_service.domain.orders.dtos.OrderDto;

@Mapper
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    Order toEntity(CreateOrderDto dto);

    @Mapping(
        source = "asset.ticker",
        target = "assetTicker"
    )
    OrderDto toDto(Order order);
}
