package rs.banka4.bank_service.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import rs.banka4.bank_service.domain.account.db.Account;
import rs.banka4.bank_service.domain.options.db.Option;
import rs.banka4.bank_service.domain.orders.db.Direction;
import rs.banka4.bank_service.domain.orders.db.Order;
import rs.banka4.bank_service.domain.orders.db.OrderType;
import rs.banka4.bank_service.domain.orders.db.Status;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.trading.db.OtcRequest;
import rs.banka4.bank_service.exceptions.user.UserNotFound;
import rs.banka4.bank_service.repositories.OrderRepository;
import rs.banka4.bank_service.repositories.UserRepository;
import rs.banka4.bank_service.service.abstraction.*;
import rs.banka4.bank_service.tx.TxExecutor;
import rs.banka4.bank_service.tx.data.*;
import rs.banka4.bank_service.tx.otc.mapper.InterbankOtcMapper;

@Service
@RequiredArgsConstructor
public class TradingServiceImpl implements TradingService {
    private final TxExecutor txExecutor;
    private final AccountService accountService;
    private final AssetOwnershipService assetOwnershipService;
    private final ExchangeRateService exchangeRateService;
    private final BankAccountService bankAccountService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    public void sendPremiumAndGetOption(OtcRequest otcRequest) {
        var monetaryAsset =
            new TxAsset.Monas(
                new MonetaryAsset(
                    otcRequest.getPremium()
                        .getCurrency()
                )
            );
        var buyer = new TxAccount.Person(otcRequest.getMadeBy());
        var seller = new TxAccount.Person(otcRequest.getMadeFor());
        Posting buyerCreditPremium =
            new Posting(
                buyer,
                otcRequest.getPremium()
                    .getAmount()
                    .negate(),
                monetaryAsset
            );
        Posting sellerDebitPremium =
            new Posting(
                seller,
                otcRequest.getPremium()
                    .getAmount(),
                monetaryAsset
            );
        var option =
            new TxAsset.Option(
                new OptionDescription(
                    ForeignBankId.our(UUID.randomUUID()),
                    new StockDescription(
                        otcRequest.getStock()
                            .getTicker()
                    ),
                    otcRequest.getPricePerStock(),
                    InterbankOtcMapper.midnightSettlementDate(otcRequest),
                    otcRequest.getAmount(),
                    otcRequest.getId()
                )
            );
        Posting buyerDebitOption = new Posting(buyer, BigDecimal.ONE, option);
        Posting sellerCreditOption = new Posting(seller, BigDecimal.ONE.negate(), option);
        txExecutor.submitTx(
            new DoubleEntryTransaction(
                List.of(
                    buyerDebitOption,
                    buyerCreditPremium,
                    sellerCreditOption,
                    sellerDebitPremium
                ),
                "🤯",
                null
            )
        );
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void buyOption(Option o, UUID userId, String userAccount, int amount) {
        Account a = accountService.getAccountByAccountNumber(userAccount);

        if (
            a.getCurrency()
                == o.getPremium()
                    .getCurrency()
        ) {
            Posting premium =
                new Posting(
                    new TxAccount.Account(userAccount),
                    o.getPremium()
                        .getAmount()
                        .multiply(new BigDecimal(amount))
                        .negate(),
                    new TxAsset.Monas(
                        new MonetaryAsset(
                            o.getPremium()
                                .getCurrency()
                        )
                    )
                );
            Posting memHole =
                new Posting(
                    new TxAccount.MemoryHole(),
                    o.getPremium()
                        .getAmount()
                        .multiply(new BigDecimal(amount)),
                    new TxAsset.Monas(
                        new MonetaryAsset(
                            o.getPremium()
                                .getCurrency()
                        )
                    )
                );
            txExecutor.submitImmediateTx(
                new DoubleEntryTransaction(List.of(premium, memHole), "radenkovic je cava", null)
            );
        } else {
            BigDecimal original =
                exchangeRateService.convertCurrency(
                    o.getPremium()
                        .getAmount()
                        .multiply(new BigDecimal(amount)),
                    o.getPremium()
                        .getCurrency(),
                    a.getCurrency()
                );
            BigDecimal withFee = original.add(exchangeRateService.calculateFee(original));
            Posting user =
                new Posting(
                    new TxAccount.Account(userAccount),
                    withFee.negate(),
                    new TxAsset.Monas(new MonetaryAsset(a.getCurrency()))
                );
            Posting bank =
                new Posting(
                    new TxAccount.Account(
                        bankAccountService.getBankAccountForCurrency(a.getCurrency())
                            .getAccountNumber()
                    ),
                    withFee,
                    new TxAsset.Monas(new MonetaryAsset(a.getCurrency()))
                );
            Posting bankOther =
                new Posting(
                    new TxAccount.Account(
                        bankAccountService.getBankAccountForCurrency(
                            o.getPremium()
                                .getCurrency()
                        )
                            .getAccountNumber()
                    ),
                    o.getPremium()
                        .getAmount()
                        .multiply(new BigDecimal(amount))
                        .negate(),
                    new TxAsset.Monas(
                        new MonetaryAsset(
                            o.getPremium()
                                .getCurrency()
                        )
                    )
                );
            Posting bankOtherMemHole =
                new Posting(
                    new TxAccount.MemoryHole(),
                    o.getPremium()
                        .getAmount()
                        .multiply(new BigDecimal(amount)),
                    new TxAsset.Monas(
                        new MonetaryAsset(
                            o.getPremium()
                                .getCurrency()
                        )
                    )
                );
            txExecutor.submitImmediateTx(
                new DoubleEntryTransaction(
                    List.of(user, bank, bankOther, bankOtherMemHole),
                    "irina izdaja",
                    null
                )
            );
        }

        assetOwnershipService.changeAssetOwnership(o.getId(), userId, amount, 0, 0);
    }

    /**
     * take option from user take stock from user get strikePrice to account comedy if currencies
     * does not match :)
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void usePutOption(Option o, UUID userId, String userAccount, int amount) {
        Account a = accountService.getAccountByAccountNumber(userAccount);
        BigDecimal strikePriceAmount =
            o.getStrikePrice()
                .getAmount()
                .multiply(BigDecimal.valueOf(amount));

        if (
            a.getCurrency()
                == o.getStrikePrice()
                    .getCurrency()
        ) {
            Posting strikePrice =
                new Posting(
                    new TxAccount.Account(userAccount),
                    strikePriceAmount,
                    new TxAsset.Monas(
                        new MonetaryAsset(
                            o.getStrikePrice()
                                .getCurrency()
                        )
                    )
                );
            Posting memHole =
                new Posting(
                    new TxAccount.MemoryHole(),
                    strikePriceAmount.negate(),
                    new TxAsset.Monas(
                        new MonetaryAsset(
                            o.getStrikePrice()
                                .getCurrency()
                        )
                    )
                );
            txExecutor.submitImmediateTx(
                new DoubleEntryTransaction(
                    List.of(strikePrice, memHole),
                    "tun tun tun tun tun tun tun sahur",
                    null
                )
            );
        } else {
            BigDecimal value =
                exchangeRateService.convertCurrency(
                    strikePriceAmount,
                    o.getStrikePrice()
                        .getCurrency(),
                    a.getCurrency()
                );
            BigDecimal fee = exchangeRateService.calculateFee(value);
            BigDecimal dajUseru = value.subtract(fee);

            Posting bankaDobija =
                new Posting(
                    new TxAccount.Account(
                        bankAccountService.getBankAccountForCurrency(
                            o.getStrikePrice()
                                .getCurrency()
                        )
                            .getAccountNumber()
                    ),
                    strikePriceAmount,
                    new TxAsset.Monas(
                        new MonetaryAsset(
                            o.getStrikePrice()
                                .getCurrency()
                        )
                    )
                );
            Posting memhole =
                new Posting(
                    new TxAccount.MemoryHole(),
                    strikePriceAmount.negate(),
                    new TxAsset.Monas(
                        new MonetaryAsset(
                            o.getStrikePrice()
                                .getCurrency()
                        )
                    )
                );
            Posting userDobija =
                new Posting(
                    new TxAccount.Account(userAccount),
                    dajUseru,
                    new TxAsset.Monas(new MonetaryAsset(a.getCurrency()))
                );
            Posting bankaDaje =
                new Posting(
                    new TxAccount.Account(
                        bankAccountService.getBankAccountForCurrency(
                            o.getStrikePrice()
                                .getCurrency()
                        )
                            .getAccountNumber()
                    ),
                    dajUseru.negate(),
                    new TxAsset.Monas(new MonetaryAsset(a.getCurrency()))
                );
            txExecutor.submitImmediateTx(
                new DoubleEntryTransaction(
                    List.of(bankaDobija, memhole, userDobija, bankaDaje),
                    "bombardiro crocodilo",
                    null
                )
            );
        }
        assetOwnershipService.changeAssetOwnership(o.getId(), userId, -amount, 0, 0);
        assetOwnershipService.changeAssetOwnership(
            o.getStock()
                .getId(),
            userId,
            -amount,
            0,
            0
        );
        var u =
            userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound(userId.toString()));
        var order =
            Order.builder()
                .user(u)
                .asset(o.getStock())
                .orderType(OrderType.MARKET)
                .quantity(amount)
                .contractSize(1)
                .pricePerUnit(o.getStrikePrice())
                .direction(Direction.SELL)
                .status(Status.APPROVED)
                .approvedBy(null)
                .isDone(true)
                .lastModified(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .remainingPortions(0)
                .afterHours(false)
                .limitValue(null)
                .stopValue(null)
                .allOrNothing(true)
                .margin(false)
                .account(a)
                .used(true)
                .build();
        orderRepository.save(order);
    }

    /**
     * take strikePrice from user give user stock take option from user comedy for currencies that
     * doesn't match
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void useCallOptionFromExchange(Option o, UUID userId, String userAccount, int amount) {
        Account a = accountService.getAccountByAccountNumber(userAccount);
        BigDecimal strikePriceAmount =
            o.getStrikePrice()
                .getAmount()
                .multiply(BigDecimal.valueOf(amount));

        if (
            a.getCurrency()
                == o.getStrikePrice()
                    .getCurrency()
        ) {
            Posting skiniSaUsera =
                new Posting(
                    new TxAccount.Account(userAccount),
                    strikePriceAmount.negate(),
                    new TxAsset.Monas(
                        new MonetaryAsset(
                            o.getStrikePrice()
                                .getCurrency()
                        )
                    )
                );
            Posting memhole =
                new Posting(
                    new TxAccount.MemoryHole(),
                    strikePriceAmount,
                    new TxAsset.Monas(
                        new MonetaryAsset(
                            o.getStrikePrice()
                                .getCurrency()
                        )
                    )
                );
            txExecutor.submitImmediateTx(
                new DoubleEntryTransaction(List.of(skiniSaUsera, memhole), "🤡", null)
            );
        } else {
            BigDecimal strikePrice =
                exchangeRateService.convertCurrency(
                    strikePriceAmount,
                    o.getStrikePrice()
                        .getCurrency(),
                    a.getCurrency()
                );
            BigDecimal takeFromUser =
                strikePrice.add(exchangeRateService.calculateFee(strikePrice));

            Posting skiniSaUsera =
                new Posting(
                    new TxAccount.Account(userAccount),
                    takeFromUser.negate(),
                    new TxAsset.Monas(new MonetaryAsset(a.getCurrency()))
                );
            Posting dajBanci =
                new Posting(
                    new TxAccount.Account(
                        bankAccountService.getBankAccountForCurrency(a.getCurrency())
                            .getAccountNumber()
                    ),
                    takeFromUser,
                    new TxAsset.Monas(new MonetaryAsset(a.getCurrency()))
                );
            Posting skiniSaBanke =
                new Posting(
                    new TxAccount.Account(
                        bankAccountService.getBankAccountForCurrency(
                            o.getStrikePrice()
                                .getCurrency()
                        )
                            .getAccountNumber()
                    ),
                    strikePriceAmount.negate(),
                    new TxAsset.Monas(
                        new MonetaryAsset(
                            o.getStrikePrice()
                                .getCurrency()
                        )
                    )
                );
            Posting memhole =
                new Posting(
                    new TxAccount.MemoryHole(),
                    strikePriceAmount,
                    new TxAsset.Monas(
                        new MonetaryAsset(
                            o.getStrikePrice()
                                .getCurrency()
                        )
                    )
                );
            txExecutor.submitImmediateTx(
                new DoubleEntryTransaction(
                    List.of(skiniSaUsera, dajBanci, skiniSaBanke, memhole),
                    "💀",
                    null
                )
            );
        }
        assetOwnershipService.changeAssetOwnership(o.getId(), userId, -amount, 0, 0);
        assetOwnershipService.changeAssetOwnership(
            o.getStock()
                .getId(),
            userId,
            amount,
            0,
            0
        );
        var u =
            userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound(userId.toString()));
        var order =
            Order.builder()
                .user(u)
                .asset(o.getStock())
                .orderType(OrderType.MARKET)
                .quantity(amount)
                .contractSize(1)
                .pricePerUnit(o.getStrikePrice())
                .direction(Direction.BUY)
                .status(Status.APPROVED)
                .approvedBy(null)
                .isDone(true)
                .lastModified(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .remainingPortions(0)
                .afterHours(false)
                .limitValue(null)
                .stopValue(null)
                .allOrNothing(true)
                .margin(false)
                .account(a)
                .used(true)
                .build();
        orderRepository.save(order);
    }

    @Override
    public void useCallOptionFromOtc(
        Option o,
        ForeignBankId buyerId,
        ForeignBankId sellerId,
        String buyerAccount,
        int amount
    ) {
        var stock =
            new TxAsset.Stock(
                new StockDescription(
                    o.getStock()
                        .getTicker()
                )
            );
        var option = new TxAccount.Option(o.getForeignId());
        Posting creditStocks = new Posting(option, new BigDecimal(-amount), stock);
        Posting debitStocks =
            new Posting(new TxAccount.Person(buyerId), new BigDecimal(amount), stock);
        BigDecimal money =
            o.getStrikePrice()
                .getAmount()
                .multiply(BigDecimal.valueOf(amount));
        var moneyAsset =
            new TxAsset.Monas(
                new MonetaryAsset(
                    o.getStrikePrice()
                        .getCurrency()
                )
            );
        Posting creditBuyer =
            new Posting(new TxAccount.Account(buyerAccount), money.negate(), moneyAsset);
        Posting debitSeller =
            new Posting(new TxAccount.Option(o.getForeignId()), money, moneyAsset);
        txExecutor.submitTx(
            new DoubleEntryTransaction(
                List.of(creditBuyer, creditStocks, debitStocks, debitSeller),
                "😴",
                null
            )
        );
        var u = userRepository.findById(UUID.fromString(buyerId.id()));
        if (u.isPresent()) {
            var a = accountService.getAccountByAccountNumber(buyerAccount);
            var order =
                Order.builder()
                    .user(u.get())
                    .asset(o.getStock())
                    .orderType(OrderType.MARKET)
                    .quantity(amount)
                    .contractSize(1)
                    .pricePerUnit(o.getStrikePrice())
                    .direction(Direction.BUY)
                    .status(Status.APPROVED)
                    .approvedBy(null)
                    .isDone(true)
                    .lastModified(OffsetDateTime.now())
                    .createdAt(OffsetDateTime.now())
                    .remainingPortions(0)
                    .afterHours(false)
                    .limitValue(null)
                    .stopValue(null)
                    .allOrNothing(true)
                    .margin(false)
                    .account(a)
                    .used(true)
                    .build();
            orderRepository.save(order);
        }
    }
}
