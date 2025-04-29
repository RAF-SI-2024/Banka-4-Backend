package rs.banka4.bank_service.unit.order;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rs.banka4.bank_service.domain.actuaries.db.ActuaryInfo;
import rs.banka4.bank_service.domain.listing.db.Listing;
import rs.banka4.bank_service.domain.options.db.Asset;
import rs.banka4.bank_service.domain.orders.db.Direction;
import rs.banka4.bank_service.domain.orders.db.Order;
import rs.banka4.bank_service.domain.orders.db.OrderType;
import rs.banka4.bank_service.domain.orders.dtos.CreateOrderDto;
import rs.banka4.bank_service.domain.orders.dtos.OrderDto;
import rs.banka4.bank_service.domain.orders.mapper.OrderMapper;
import rs.banka4.bank_service.exceptions.AssetNotFound;
import rs.banka4.bank_service.exceptions.ExchangeNotFound;
import rs.banka4.bank_service.generator.ActuaryObjectMother;
import rs.banka4.bank_service.generator.AssetObjectMother;
import rs.banka4.bank_service.generator.EmployeeObjectMother;
import rs.banka4.bank_service.generator.ListingObjectMother;
import rs.banka4.bank_service.generator.OrderObjectMother;
import rs.banka4.bank_service.repositories.ActuaryRepository;
import rs.banka4.bank_service.repositories.AssetRepository;
import rs.banka4.bank_service.repositories.OrderRepository;
import rs.banka4.bank_service.repositories.UserRepository;
import rs.banka4.bank_service.service.abstraction.AccountService;
import rs.banka4.bank_service.service.abstraction.ListingService;
import rs.banka4.bank_service.service.impl.OrderServiceImpl;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserPrincipal;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.rafeisen.common.security.UserType;

public class OrderServiceCreateOrderTests {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private ActuaryRepository actuaryRepository;
    @Mock
    private ListingService listingService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountService accountService;
    @InjectMocks
    private OrderServiceImpl orderService;


    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.fromString("35bc1ef6-f6d0-4405-bcdb-7dc0686b7b87");
    }

    static Stream<Arguments> orderParameters() {
        return Stream.of(
            Arguments.of(
                "Market Order",
                (Supplier<CreateOrderDto>) () -> OrderObjectMother.generateBasicCreateOrderDto(
                    Direction.BUY
                ),
                OrderType.MARKET
            ),
            Arguments.of(
                "Limit Order",
                (Supplier<CreateOrderDto>) OrderObjectMother::generateBasicCreateLimitOrderDto,
                OrderType.LIMIT
            ),
            Arguments.of(
                "Stop Order",
                (Supplier<CreateOrderDto>) OrderObjectMother::generateBasicCreateStopOrderDto,
                OrderType.STOP
            )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("orderParameters")
    @Disabled
    void testCreateOrderSuccessParameterized(
        String testName,
        Supplier<CreateOrderDto> dtoSupplier,
        OrderType expectedOrderType
    ) {
        // Arrange
        CreateOrderDto dto = dtoSupplier.get();
        OrderDto orderDto = OrderObjectMother.generateBasicOrderDto(expectedOrderType);
        Asset asset = AssetObjectMother.generateBasicStock();
        Listing listing = ListingObjectMother.generateBasicListing();
        final var employee = EmployeeObjectMother.generateBasicEmployee();
        ActuaryInfo actuaryInfo = ActuaryObjectMother.generateBasicActuaryInfo(employee);
        Order order = OrderObjectMother.generateBasicOrder(employee, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(employee));
        when(assetRepository.findById(dto.assetId())).thenReturn(Optional.of(asset));
        when(actuaryRepository.findByUserId(userId)).thenReturn(Optional.of(actuaryInfo));
        when(listingService.findActiveListingByAsset(asset.getId())).thenReturn(
            Optional.of(listing)
        );
        when(orderMapper.toEntity(dto)).thenReturn(order);
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(
            invocation -> invocation.getArgument(0)
        );
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        // Act
        OrderDto createdOrder =
            orderService.createOrder(
                dto,
                userId,
                new AuthenticatedBankUserAuthentication(
                    new AuthenticatedBankUserPrincipal(UserType.CLIENT, userId),
                    "",
                    EnumSet.noneOf(Privilege.class)
                )
            );

        // Assert
        verify(assetRepository).findById(dto.assetId());
        assertThat(createdOrder.orderType()).isEqualTo(expectedOrderType);
    }

    @Test
    void testCreateOrderWithMissingAsset() {
        // Arrange
        CreateOrderDto dto = OrderObjectMother.generateBasicCreateOrderDto(Direction.BUY);

        when(assetRepository.findById(dto.assetId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            AssetNotFound.class,
            () -> orderService.createOrder(
                dto,
                userId,
                new AuthenticatedBankUserAuthentication(
                    new AuthenticatedBankUserPrincipal(UserType.CLIENT, userId),
                    "",
                    EnumSet.noneOf(Privilege.class)
                )
            )
        );
    }

    @Test
    void testCreateOrderWithMissingExchange() {
        // Arrange
        CreateOrderDto dto = OrderObjectMother.generateBasicCreateOrderDto(Direction.BUY);
        final var employee = EmployeeObjectMother.generateBasicEmployee();
        ActuaryInfo actuaryInfo = ActuaryObjectMother.generateBasicActuaryInfo(employee);
        Asset asset = AssetObjectMother.generateBasicStock();

        when(assetRepository.findById(dto.assetId())).thenReturn(Optional.of(asset));
        when(actuaryRepository.findByUserId(userId)).thenReturn(Optional.of(actuaryInfo));
        when(listingService.findActiveListingByAsset(asset.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ExchangeNotFound.class,
            () -> orderService.createOrder(
                dto,
                userId,
                new AuthenticatedBankUserAuthentication(
                    new AuthenticatedBankUserPrincipal(UserType.CLIENT, userId),
                    "",
                    EnumSet.noneOf(Privilege.class)
                )
            )
        );
    }

    @Test
    void testCreateOrderWithActuaryNotFound() {
        // Arrange
        CreateOrderDto dto = OrderObjectMother.generateBasicCreateOrderDto(Direction.BUY);
        Asset asset = AssetObjectMother.generateBasicStock();

        when(assetRepository.findById(dto.assetId())).thenReturn(Optional.of(asset));
        when(actuaryRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            RuntimeException.class,
            () -> orderService.createOrder(
                dto,
                userId,
                new AuthenticatedBankUserAuthentication(
                    new AuthenticatedBankUserPrincipal(UserType.CLIENT, userId),
                    "",
                    EnumSet.noneOf(Privilege.class)
                )
            )
        );
    }

    @Test
    @Disabled
    void testCreateOrderSuccessSellOrder() {
        // Arrange
        CreateOrderDto dto = OrderObjectMother.generateBasicCreateOrderDto(Direction.SELL);
        OrderDto orderDto = OrderObjectMother.generateBasicOrderDto(OrderType.MARKET);
        Asset asset = AssetObjectMother.generateBasicStock();
        Listing listing = ListingObjectMother.generateBasicListing();
        final var employee = EmployeeObjectMother.generateBasicEmployee();
        ActuaryInfo actuaryInfo = ActuaryObjectMother.generateBasicActuaryInfo(employee);
        Order order = OrderObjectMother.generateBasicOrder(employee, null);


        when(userRepository.findById(userId)).thenReturn(Optional.of(employee));
        when(assetRepository.findById(dto.assetId())).thenReturn(Optional.of(asset));
        when(actuaryRepository.findByUserId(userId)).thenReturn(Optional.of(actuaryInfo));
        when(listingService.findActiveListingByAsset(asset.getId())).thenReturn(
            Optional.of(listing)
        );
        when(orderMapper.toEntity(dto)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(
            invocation -> invocation.getArgument(0)
        );

        // Act
        OrderDto createdOrder =
            orderService.createOrder(
                dto,
                userId,
                new AuthenticatedBankUserAuthentication(
                    new AuthenticatedBankUserPrincipal(UserType.CLIENT, userId),
                    "",
                    EnumSet.noneOf(Privilege.class)
                )
            );

        // Assert
        verify(assetRepository).findById(dto.assetId());
        assertThat(createdOrder.direction()).isEqualTo(Direction.SELL);
    }
}
