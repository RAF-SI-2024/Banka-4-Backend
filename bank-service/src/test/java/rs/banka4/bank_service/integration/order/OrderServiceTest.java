package rs.banka4.bank_service.integration.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.exchanges.db.Exchange;
import rs.banka4.bank_service.domain.options.db.Asset;
import rs.banka4.bank_service.domain.orders.db.Direction;
import rs.banka4.bank_service.domain.orders.db.OrderType;
import rs.banka4.bank_service.domain.orders.db.Status;
import rs.banka4.bank_service.domain.orders.dtos.CreateOrderDto;
import rs.banka4.bank_service.domain.orders.dtos.CreateOrderPreviewDto;
import rs.banka4.bank_service.domain.orders.dtos.OrderDto;
import rs.banka4.bank_service.domain.orders.dtos.OrderPreviewDto;
import rs.banka4.bank_service.repositories.AssetRepository;
import rs.banka4.bank_service.repositories.ExchangeRepository;
import rs.banka4.bank_service.repositories.ListingRepository;
import rs.banka4.bank_service.repositories.OrderRepository;
import rs.banka4.bank_service.service.abstraction.OrderService;
import rs.banka4.bank_service.utils.DataSourceService;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserPrincipal;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.rafeisen.common.security.UserType;
import rs.banka4.testlib.integration.DbEnabledTest;

@SpringBootTest
@DbEnabledTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private ExchangeRepository exchangeRepository;
    @Autowired
    private ListingRepository listingRepository;
    @Autowired
    private DataSourceService dataSourceService;

    private static final UUID CLIENT_ID = DataSourceService.CLIENT_JANE;
    private static final String ACCOUNT_NUMBER = DataSourceService.ACCOUNT_JANE_STANDARD_NUMBER;

    private static final UUID ASSET_ID = TestDataFactory.ASSET_ID;

    @BeforeEach
    void setUp() {
        dataSourceService.insertData(true);

        orderRepository.deleteAll();
        Asset asset = TestDataFactory.buildAsset();
        assetRepository.save(asset);
        Exchange exchange = TestDataFactory.buildExchange();
        exchangeRepository.save(exchange);
        listingRepository.save(TestDataFactory.buildListing(asset, exchange));
    }

    @Test
    void shouldCreateOrder() {
        CreateOrderDto dto =
            new CreateOrderDto(
                ASSET_ID,
                Direction.BUY,
                2,
                new rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount(
                    BigDecimal.valueOf(950),
                    CurrencyCode.RSD
                ),
                null,
                false,
                false,
                ACCOUNT_NUMBER
            );

        OrderDto response =
            orderService.createOrder(
                dto,
                CLIENT_ID,
                new AuthenticatedBankUserAuthentication(
                    new AuthenticatedBankUserPrincipal(UserType.CLIENT, CLIENT_ID),
                    "",
                    EnumSet.noneOf(Privilege.class)
                )
            );

        assertThat(response).isNotNull();
        assertThat(response.assetTicker()).isEqualTo("TST");
    }

    @Test
    void shouldCalculateAveragePrice() {
        CreateOrderPreviewDto dto =
            new CreateOrderPreviewDto(
                ASSET_ID,
                3,
                new MonetaryAmount(BigDecimal.valueOf(900), CurrencyCode.RSD),
                null,
                false,
                false,
                Direction.BUY,
                ACCOUNT_NUMBER
            );

        OrderPreviewDto response = orderService.calculateAveragePrice(dto);

        assertThat(response).isNotNull();
        assertThat(response.quantity()).isEqualTo(3);
        assertThat(response.orderType()).contains("Limit Order");
    }

    @Test
    void shouldReturnOrderByIdFromService() {
        CreateOrderDto dto =
            new CreateOrderDto(
                ASSET_ID,
                Direction.BUY,
                5,
                new MonetaryAmount(BigDecimal.valueOf(100), CurrencyCode.RSD),
                null,
                false,
                false,
                ACCOUNT_NUMBER
            );

        OrderDto created =
            orderService.createOrder(
                dto,
                CLIENT_ID,
                new AuthenticatedBankUserAuthentication(
                    new AuthenticatedBankUserPrincipal(UserType.CLIENT, CLIENT_ID),
                    "",
                    EnumSet.noneOf(Privilege.class)
                )
            );

        OrderDto foundOrder = orderService.getOrderById(created.id());

        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.id()).isEqualTo(created.id());
        assertThat(foundOrder.orderType()).isEqualTo(OrderType.LIMIT);
        assertThat(foundOrder.status()).isEqualTo(Status.APPROVED);
        assertThat(foundOrder.quantity()).isEqualTo(5);
    }
}
