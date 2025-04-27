package rs.banka4.bank_service.integration.generator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rs.banka4.bank_service.domain.actuaries.db.ActuaryInfo;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.assets.db.AssetOwnership;
import rs.banka4.bank_service.domain.assets.db.AssetOwnershipId;
import rs.banka4.bank_service.domain.options.db.Asset;
import rs.banka4.bank_service.domain.orders.db.Direction;
import rs.banka4.bank_service.domain.orders.db.Order;
import rs.banka4.bank_service.domain.orders.db.OrderType;
import rs.banka4.bank_service.domain.orders.db.Status;
import rs.banka4.bank_service.domain.taxes.db.UserTaxDebts;
import rs.banka4.bank_service.domain.user.User;
import rs.banka4.bank_service.domain.user.client.db.Client;
import rs.banka4.bank_service.generator.AccountObjectMother;
import rs.banka4.bank_service.repositories.*;
import rs.banka4.bank_service.utils.ActuaryGenerator;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.testlib.utils.JwtPlaceholders;

@Component
public class PortfolioGenerator {
    @Autowired
    private UserGenerator userGen;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserTaxDebtsRepository userTaxDebtsRepository;
    @Autowired
    private AssetOwnershipRepository assetOwnershipRepository;
    @Autowired
    private OrderRepository orderRepository;

    public void createDummyTax(Client client) {
        var account = AccountObjectMother.generateBasicToAccount();
        account.setClient(client);
        userRepository.save(account.getEmployee());
        accountRepository.save(account);
        var dept =
            UserTaxDebts.builder()
                .debtAmount(BigDecimal.valueOf(100))
                .yearlyDebtAmount(BigDecimal.valueOf(1000))
                .account(account)
                .build();
        userTaxDebtsRepository.save(dept);
    }

    public void createDummyTaxEur(Client client) {
        var account = AccountObjectMother.generateBasicEURFromAccount();
        account.setClient(client);
        userRepository.save(account.getEmployee());
        accountRepository.save(account);
        var dept =
            UserTaxDebts.builder()
                .debtAmount(BigDecimal.valueOf(50))
                .yearlyDebtAmount(BigDecimal.valueOf(200))
                .account(account)
                .build();
        userTaxDebtsRepository.save(dept);
    }

    public void createDummyAssetOwnership(
        User userId,
        Asset asset,
        int privateAmt,
        int publicAmt,
        int reservedAmt
    ) {
        AssetOwnershipId ownershipId = new AssetOwnershipId(userId, asset);
        AssetOwnership ownership = new AssetOwnership();
        ownership.setId(ownershipId);
        ownership.setPrivateAmount(privateAmt);
        ownership.setPublicAmount(publicAmt);
        ownership.setReservedAmount(reservedAmt);
        assetOwnershipRepository.save(ownership);
    }

    public void createDummyAssetOwnership2(
        User userId,
        Asset asset,
        int privateAmt,
        int publicAmt,
        int reservedAmt
    ) {
        AssetOwnershipId ownershipId = new AssetOwnershipId(userId, asset);
        AssetOwnership ownership = new AssetOwnership();
        ownership.setId(ownershipId);
        ownership.setPrivateAmount(privateAmt);
        ownership.setPublicAmount(publicAmt);
        ownership.setReservedAmount(reservedAmt);
        assetOwnershipRepository.save(ownership);
    }

    public Order createDummyBuyOrder(
        Client userId,
        Asset asset,
        int quantity,
        BigDecimal priceValue,
        CurrencyCode currency
    ) {
        var account = AccountObjectMother.generateBasicToAccount();
        account.setClient(userId);
        userRepository.save(account.getEmployee());
        accountRepository.save(account);
        Order buyOrder =
            Order.builder()
                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .user(userId)
                .asset(asset)
                .orderType(OrderType.MARKET)
                .quantity(quantity)
                .contractSize(1)
                .pricePerUnit(new MonetaryAmount(priceValue, currency))
                .direction(Direction.BUY)
                .status(Status.APPROVED)
                .approvedBy(null)
                .isDone(true)
                .lastModified(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .remainingPortions(100)
                .afterHours(false)
                .limitValue(null)
                .stopValue(null)
                .allOrNothing(false)
                .margin(false)
                .account(account)
                .used(false)
                .build();
        return orderRepository.save(buyOrder);
    }

    public Order createDummyBuyOrder2(
        Client userId,
        Asset asset,
        int quantity,
        BigDecimal priceValue,
        CurrencyCode currency
    ) {
        var account = AccountObjectMother.generateBasicFromAccount();
        account.setClient(userId);
        userRepository.save(account.getEmployee());
        accountRepository.save(account);
        Order buyOrder =
            Order.builder()
                .user(userId)
                .asset(asset)
                .orderType(OrderType.MARKET)
                .quantity(quantity)
                .contractSize(1)
                .pricePerUnit(new MonetaryAmount(priceValue, currency))
                .direction(Direction.BUY)
                .status(Status.APPROVED)
                .approvedBy(null)
                .isDone(true)
                .lastModified(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .remainingPortions(100)
                .afterHours(false)
                .limitValue(null)
                .stopValue(null)
                .allOrNothing(false)
                .margin(false)
                .account(account)
                .used(false)
                .build();
        return orderRepository.save(buyOrder);
    }

    public void createDummyBuyOrderSTOP(
        Client userId,
        Asset asset,
        int quantity,
        BigDecimal priceValue,
        CurrencyCode currency
    ) {
        var account = AccountObjectMother.generateBasicToAccount();
        account.setClient(userId);
        userRepository.save(account.getEmployee());
        accountRepository.save(account);
        Order buyOrder =
            Order.builder()
                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .user(userId)
                .asset(asset)
                .orderType(OrderType.STOP)
                .quantity(quantity)
                .contractSize(1)
                .pricePerUnit(new MonetaryAmount(priceValue, currency))
                .direction(Direction.BUY)
                .status(Status.APPROVED)
                .approvedBy(null)
                .isDone(true)
                .lastModified(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .remainingPortions(100)
                .afterHours(false)
                .limitValue(null)
                .stopValue(null)
                .allOrNothing(false)
                .margin(false)
                .account(account)
                .used(false)
                .build();
        orderRepository.save(buyOrder);
    }

    public Order createDummySellOrder(
        Client userId,
        Asset asset,
        int quantity,
        BigDecimal priceValue,
        CurrencyCode currency
    ) {
        var account = AccountObjectMother.generateBasicEURFromAccount();
        account.setClient(userId);
        account.setAccountNumber(
            UUID.randomUUID()
                .toString()
        );
        userRepository.save(account.getEmployee());
        accountRepository.save(account);
        Order buyOrder =
            Order.builder()
                .user(userId)
                .asset(asset)
                .orderType(OrderType.MARKET)
                .quantity(quantity)
                .contractSize(1)
                .pricePerUnit(new MonetaryAmount(priceValue, currency))
                .direction(Direction.SELL)
                .status(Status.APPROVED)
                .approvedBy(null)
                .isDone(true)
                .lastModified(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .remainingPortions(100)
                .afterHours(false)
                .limitValue(null)
                .stopValue(null)
                .allOrNothing(false)
                .margin(false)
                .account(account)
                .used(false)
                .build();
        return orderRepository.save(buyOrder);
    }

    public Client createTestClient() {
        final var assetOwner =
            userGen.createClient(
                x -> x.id(JwtPlaceholders.CLIENT_ID)
                    .email("johndqoe@example.com")
            );
        return userRepository.save(assetOwner);
    }

    public Client createTestClientDiffIf() {
        final var assetOwner =
            userGen.createClient(
                x -> x.id(UUID.randomUUID())
                    .email("johndqoe@example.com")
            );
        return userRepository.save(assetOwner);
    }

    public List<ActuaryInfo> createTestActuary() {
        return ActuaryGenerator.makeExampleActuaries(userGen);
    }
}
