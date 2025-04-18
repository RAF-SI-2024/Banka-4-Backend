package rs.banka4.bank_service.integration.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import rs.banka4.bank_service.domain.account.db.Account;
import rs.banka4.bank_service.domain.actuaries.db.ActuaryInfo;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.exchanges.db.Exchange;
import rs.banka4.bank_service.domain.listing.db.Listing;
import rs.banka4.bank_service.domain.options.db.Asset;
import rs.banka4.bank_service.domain.orders.db.Direction;
import rs.banka4.bank_service.domain.orders.db.Order;
import rs.banka4.bank_service.domain.orders.db.OrderType;
import rs.banka4.bank_service.domain.orders.db.Status;
import rs.banka4.bank_service.domain.security.Security;
import rs.banka4.bank_service.domain.security.stock.db.Stock;
import rs.banka4.bank_service.domain.user.User;
import rs.banka4.rafeisen.common.currency.CurrencyCode;

public class TestDataFactory {

    public static final UUID EXCHANGE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID ASSET_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000009");
    public static final UUID LISTING_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    public static final UUID ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    public static final String ACCOUNT_NUMBER = "4440001000000000220";

    public static Stock buildAsset() {
        return Stock.builder()
            .id(ASSET_ID)
            .name("Test Stock")
            .ticker("TST")
            .outstandingShares(1_000_000L)
            .dividendYield(BigDecimal.valueOf(0.03))
            .build();
    }

    public static Exchange buildExchange() {
        return Exchange.builder()
            .id(EXCHANGE_ID)
            .exchangeName("Belex")
            .exchangeAcronym("BLX")
            .exchangeMICCode("XBELEX")
            .polity("Serbia")
            .currency(CurrencyCode.RSD)
            .timeZone("Europe/Belgrade")
            .openTime(
                OffsetDateTime.now()
                    .minusHours(2)
            )
            .closeTime(
                OffsetDateTime.now()
                    .plusHours(2)
            )
            .build();
    }

    public static ActuaryInfo buildActuaryInfo(User user) {
        return ActuaryInfo.builder()
            .userId(user.getId())
            .needApproval(true)
            .limit(new MonetaryAmount(BigDecimal.valueOf(1_000_000), CurrencyCode.RSD))
            .usedLimit(new MonetaryAmount(BigDecimal.valueOf(1000), CurrencyCode.RSD))
            .build();
    }

    public static Listing buildListing(Asset asset, Exchange exchange) {
        return Listing.builder()
            .id(LISTING_ID)
            .security((Security) asset)
            .exchange(exchange)
            .lastRefresh(OffsetDateTime.now())
            .bid(BigDecimal.valueOf(800))
            .ask(BigDecimal.valueOf(1000))
            .contractSize(1)
            .active(true)
            .build();
    }

    public static Order buildOrder(User user, Account account) {
        return Order.builder()
            .id(ORDER_ID)
            .asset(buildAsset())
            .direction(Direction.BUY)
            .quantity(100)
            .account(account)
            .user(user)
            .orderType(OrderType.MARKET)
            .status(Status.PENDING)
            .pricePerUnit(new MonetaryAmount(new BigDecimal(100), CurrencyCode.RSD))
            .build();
    }
}
