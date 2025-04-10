package rs.banka4.stock_service.integration.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;
import rs.banka4.stock_service.domain.options.db.Asset;
import rs.banka4.stock_service.domain.orders.db.Direction;
import rs.banka4.stock_service.domain.orders.dtos.CreateOrderDto;
import rs.banka4.stock_service.domain.orders.dtos.CreateOrderPreviewDto;
import rs.banka4.stock_service.domain.orders.dtos.OrderDto;
import rs.banka4.stock_service.domain.orders.dtos.OrderPreviewDto;
import rs.banka4.stock_service.repositories.*;
import rs.banka4.stock_service.service.abstraction.OrderService;
import rs.banka4.testlib.integration.DbEnabledTest;

@SpringBootTest
@DbEnabledTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private ExchangeRepository exchangeRepository;
    @Autowired
    private ListingRepository listingRepository;
    @Autowired
    private ActuaryRepository actuaryRepository;

    private UUID assetId = TestDataFactory.ASSET_ID;
    private UUID accountId = TestDataFactory.ACCOUNT_ID;

    @BeforeEach
    void setUp() {
        Exchange exchange = exchangeRepository.save(TestDataFactory.buildExchange());
        Asset asset = assetRepository.save(TestDataFactory.buildAsset());
        actuaryRepository.save(TestDataFactory.buildActuaryInfo());
        listingRepository.save(TestDataFactory.buildListing(asset, exchange));
    }

    @Test
    void shouldCreateOrder() {
        CreateOrderDto dto =
            new CreateOrderDto(
                assetId,
                Direction.BUY,
                2,
                new MonetaryAmount(BigDecimal.valueOf(950), CurrencyCode.RSD),
                null,
                false,
                false,
                accountId
            );

        OrderDto response = orderService.createOrder(dto, TestDataFactory.USER_ID);

        assertThat(response).isNotNull();
        assertThat(
            response.pricePerUnit()
                .getAmount()
        ).isEqualTo(BigDecimal.valueOf(950));
        assertThat(response.assetTicker()).isEqualTo("TST");
    }

    @Test
    void shouldCalculateAveragePrice() {
        CreateOrderPreviewDto dto =
            new CreateOrderPreviewDto(
                assetId,
                3,
                new MonetaryAmount(BigDecimal.valueOf(900), CurrencyCode.RSD),
                null,
                false,
                false,
                Direction.BUY
            );

        OrderPreviewDto response = orderService.calculateAveragePrice(dto);

        assertThat(response).isNotNull();
        assertThat(response.approximatePrice()).isEqualTo(BigDecimal.valueOf(2700));
        assertThat(response.orderType()).contains("Limit Order");
    }

}
