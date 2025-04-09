package rs.banka4.stock_service.domain.orders.dtos;


import java.math.BigDecimal;

public record OrderPreviewDto(
    String orderType,
    BigDecimal approximatePrice,
    int quantity
) {
}
