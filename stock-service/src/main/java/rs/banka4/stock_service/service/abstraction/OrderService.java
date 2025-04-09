package rs.banka4.stock_service.service.abstraction;


import java.util.UUID;
import rs.banka4.stock_service.domain.orders.dtos.CreateOrderDto;
import rs.banka4.stock_service.domain.orders.dtos.CreateOrderPreviewDto;
import rs.banka4.stock_service.domain.orders.dtos.OrderDto;
import rs.banka4.stock_service.domain.orders.dtos.OrderPreviewDto;

public interface OrderService {

    OrderDto createOrder(CreateOrderDto request, UUID userId);

    OrderPreviewDto calculateAveragePrice(CreateOrderPreviewDto request);

}
