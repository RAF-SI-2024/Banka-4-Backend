package rs.banka4.bank_service.tx.otc.service.impl;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import rs.banka4.bank_service.domain.assets.db.AssetOwnership;
import rs.banka4.bank_service.domain.assets.mappers.AssetMapper;
import rs.banka4.bank_service.domain.listing.dtos.SecurityType;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.trading.db.OtcRequest;
import rs.banka4.bank_service.domain.trading.db.RequestStatus;
import rs.banka4.bank_service.domain.trading.dtos.PublicStocksDto;
import rs.banka4.bank_service.exceptions.AssetNotFound;
import rs.banka4.bank_service.exceptions.OtcNotFoundException;
import rs.banka4.bank_service.exceptions.RequestFailed;
import rs.banka4.bank_service.exceptions.WrongTurn;
import rs.banka4.bank_service.repositories.AssetOwnershipRepository;
import rs.banka4.bank_service.repositories.OtcRequestRepository;
import rs.banka4.bank_service.repositories.StockRepository;
import rs.banka4.bank_service.service.abstraction.ForeignBankService;
import rs.banka4.bank_service.service.abstraction.ListingService;
import rs.banka4.bank_service.service.abstraction.TradingService;
import rs.banka4.bank_service.tx.data.*;
import rs.banka4.bank_service.tx.data.OtcOffer;
import rs.banka4.bank_service.tx.data.PublicStock;
import rs.banka4.bank_service.tx.data.Seller;
import rs.banka4.bank_service.tx.data.StockDescription;
import rs.banka4.bank_service.tx.otc.config.InterbankRetrofitProvider;
import rs.banka4.bank_service.tx.otc.mapper.InterbankOtcMapper;
import rs.banka4.bank_service.tx.otc.service.InterbankOtcService;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterbankOtcServiceImpl implements InterbankOtcService {
    private final StockRepository stockRepository;
    private final AssetOwnershipRepository assetOwnershipRepository;
    private final InterbankRetrofitProvider interbankRetrofit;
    private final ListingService listingService;
    private final OtcRequestRepository otcRequestRepository;
    private final TradingService tradingService;
    private final ForeignBankService foreignBankService;

    /**
     * Fetches all asset ownership from our bank, filters only ones that have publicAmount > 0 and
     * sorts by stock.ticker(). Goes through the resulted list and makes required response structure
     */
    @Override
    public List<PublicStock> sendPublicStocks() {
        var ownerships = assetOwnershipRepository.findAll();
        var publicOwnerships =
            ownerships.stream()
                .filter(ow -> ow.getPublicAmount() > 0)
                .sorted(Comparator.comparing(AssetOwnership::getTicker))
                .toList();
        List<PublicStock> publicStocks = new ArrayList<>();
        for (int i = 0; i < publicOwnerships.size(); i++) {
            if (
                i == 0
                    || !publicOwnerships.get(i)
                        .getTicker()
                        .equals(
                            publicOwnerships.get(i - 1)
                                .getTicker()
                        )
            ) {
                PublicStock ps =
                    new PublicStock(
                        new StockDescription(
                            publicOwnerships.get(i)
                                .getTicker()
                        ),
                        List.of(
                            new Seller(
                                new ForeignBankId(
                                    444,
                                    publicOwnerships.get(i)
                                        .getId()
                                        .getUser()
                                        .getId()
                                        .toString()
                                ),
                                publicOwnerships.get(i)
                                    .getPublicAmount()
                            )
                        )
                    );
                publicStocks.add(ps);
            } else {
                publicStocks.getLast()
                    .sellers()
                    .add(
                        new Seller(
                            new ForeignBankId(
                                444,
                                publicOwnerships.get(i)
                                    .getId()
                                    .getUser()
                                    .getId()
                                    .toString()
                            ),
                            publicOwnerships.get(i)
                                .getPublicAmount()
                        )
                    );
            }
        }
        return publicStocks;
    }

    @Override
    public List<PublicStock> fetchPublicStocks() {
        return interbankRetrofit.getAll()
            .stream()
            .flatMap(interbank -> {
                try {
                    var call = interbank.getPublicStocks();
                    Response<List<PublicStock>> response = call.execute();
                    return response.body()
                        .stream();
                } catch (IOException e) {
                    throw new RequestFailed();
                }
            })
            .toList();
    }

    @Override
    public ForeignBankId createOtc(OtcOffer offer) {
        var stock =
            stockRepository.findByTicker(
                offer.stock()
                    .ticker()
            );
        if (stock.isEmpty()) {
            throw new AssetNotFound();
        }
        OtcRequest otcRequest = InterbankOtcMapper.INSTANCE.toOtcRequest(offer);
        ForeignBankId id =
            new ForeignBankId(
                ForeignBankId.OUR_ROUTING_NUMBER,
                UUID.randomUUID()
                    .toString()
            );
        otcRequest.setId(id);
        otcRequest.setStatus(RequestStatus.ACTIVE);
        otcRequest.setStock(stock.get());
        otcRequestRepository.save(otcRequest);
        return id;
    }

    @Override
    public void sendCreateOtc(OtcOffer offer) {
        try {
            var call =
                interbankRetrofit.get(
                    offer.sellerId()
                        .routingNumber()
                )
                    .sendCreateOtc(offer);
            Response<ForeignBankId> response = call.execute();
            ForeignBankId id = response.body();

            var stock =
                stockRepository.findByTicker(
                    offer.stock()
                        .ticker()
                );
            if (stock.isEmpty()) {
                throw new AssetNotFound();
            }
            OtcRequest otcRequest = InterbankOtcMapper.INSTANCE.toOtcRequest(offer);
            otcRequest.setId(id);
            otcRequest.setStatus(RequestStatus.ACTIVE);
            otcRequest.setStock(stock.get());
            otcRequestRepository.save(otcRequest);
        } catch (IOException e) {
            throw new RequestFailed();
        }
    }

    @Override
    public void updateOtc(OtcOffer offer, ForeignBankId id) {
        var otc = otcRequestRepository.findById(id);
        if (otc.isEmpty()) throw new OtcNotFoundException(id);
        var request = otc.get();
        if (
            request.getModifiedBy()
                .equals(offer.lastModifiedBy())
        ) throw new WrongTurn();
        request = InterbankOtcMapper.INSTANCE.toOtcRequest(offer);
        otcRequestRepository.save(request);
    }

    @Override
    public void sendUpdateOtc(OtcOffer offer, ForeignBankId id, long routingNumber) {
        try {
            var call =
                interbankRetrofit.get(routingNumber)
                    .sendUpdateOtc(offer, id.routingNumber(), id.id());
            var response = call.execute();
            if (!response.isSuccessful()) throw new WrongTurn();
        } catch (IOException e) {
            throw new RequestFailed();
        }
    }

    @Override
    public OtcNegotiation getOtcNegotiation(ForeignBankId id) {
        var xd = otcRequestRepository.findById(id);
        if (xd.isEmpty()) throw new OtcNotFoundException(id);

        return InterbankOtcMapper.INSTANCE.toOtcNegotiation(
            xd.get(),
            xd.get()
                .getStatus()
                == RequestStatus.ACTIVE
        );
    }

    @Override
    public OtcNegotiation sendGetOtcNegotiation(ForeignBankId id) {
        try {
            var call =
                interbankRetrofit.get(id.routingNumber())
                    .sendGetOtcNegotiation(id.routingNumber(), id.id());
            var response = call.execute();
            return response.body();
        } catch (IOException e) {
            throw new RequestFailed();
        }
    }

    @Override
    public void closeNegotiation(ForeignBankId id) {
        var xd = otcRequestRepository.findById(id);
        if (xd.isEmpty()) throw new OtcNotFoundException(id);
        var otc = xd.get();
        otc.setStatus(RequestStatus.REJECTED);
        otcRequestRepository.save(xd.get());
    }

    @Override
    public void sendCloseNegotiation(ForeignBankId id, long routingNumber) {
        try {
            var call =
                interbankRetrofit.get(routingNumber)
                    .closeNegotiation(id.routingNumber(), id.id());
            var response = call.execute();
            if (!response.isSuccessful()) throw new OtcNotFoundException(id);
        } catch (IOException e) {
            throw new RequestFailed();
        }
    }

    @Override
    public void acceptNegotiation(ForeignBankId id) {
        var otc = otcRequestRepository.findById(id);
        if (otc.isEmpty()) throw new OtcNotFoundException(id);
        if (
            otc.get()
                .getModifiedBy()
                .routingNumber()
                != ForeignBankId.OUR_ROUTING_NUMBER
        ) throw new WrongTurn();
        tradingService.sendPremiumAndGetOption(otc.get());
    }

    @Override
    public void sendAcceptNegotiation(ForeignBankId id, long routingNumber) {
        try {
            var call =
                interbankRetrofit.get(routingNumber)
                    .acceptNegotiation(id.routingNumber(), id.id());
            var response = call.execute();
            if (!response.isSuccessful()) throw new OtcNotFoundException(id);


        } catch (IOException e) {
            throw new RequestFailed();
        }
    }

    private List<PublicStocksDto> convertToPublicStocksDto(PublicStock dto) {
        List<PublicStocksDto> publicStocksDtos = new ArrayList<>();
        final var stock =
            stockRepository.findByTicker(
                dto.stock()
                    .ticker()
            )
                .orElseThrow(AssetNotFound::new);
        final var latestPrice = listingService.getLatestPriceForStock(stock.getId());
        for (var x : dto.sellers()) {
            try {
                var userInfo = foreignBankService.getUserInfoFor(x.seller());
                publicStocksDtos.add(
                    new PublicStocksDto(
                        SecurityType.STOCK,
                        x.seller(),
                        stock.getId(),
                        userInfo.map(UserInformation::displayName)
                            .orElse(null),
                        stock.getTicker(),
                        stock.getName(),
                        x.amount(),
                        latestPrice,
                        OffsetDateTime.now()
                    )
                );
            } catch (IOException error) {
                log.error("failed to resolve username for {}", x.seller(), error);
                throw new RequestFailed();
            }
        }
        return publicStocksDtos;
    }

    @Override
    public List<PublicStocksDto> getPublicStocks(Pageable pageable, String token) {
        final var allPublic = assetOwnershipRepository.findAllByPublicAmountGreaterThan(0);
        var otherBank = fetchPublicStocks();
        List<PublicStocksDto> publicStocksOtherBank = new ArrayList<>();
        for (var x : otherBank) {
            publicStocksOtherBank.addAll(convertToPublicStocksDto(x));
        }

        List<PublicStocksDto> ourPublicStocks = new ArrayList<>();

        for (var s : allPublic) {
            final var lastPrice =
                listingService.getLatestPriceForStock(
                    s.getId()
                        .getAsset()
                        .getId()
                );
            ourPublicStocks.add(
                AssetMapper.INSTANCE.mapPublicStocksDto(
                    s,
                    SecurityType.STOCK,
                    s.getId()
                        .getUser()
                        .getEmail(),
                    lastPrice,
                    OffsetDateTime.now()
                )
            );
        }

        publicStocksOtherBank.addAll(ourPublicStocks);
        return publicStocksOtherBank;
    }
}
