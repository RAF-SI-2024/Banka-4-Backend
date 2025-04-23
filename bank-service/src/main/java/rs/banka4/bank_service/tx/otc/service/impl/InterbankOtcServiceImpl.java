package rs.banka4.bank_service.tx.otc.service.impl;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import rs.banka4.bank_service.domain.assets.db.AssetOwnership;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.trading.db.OtcRequest;
import rs.banka4.bank_service.domain.trading.db.RequestStatus;
import rs.banka4.bank_service.exceptions.AssetNotFound;
import rs.banka4.bank_service.exceptions.OtcNotFoundException;
import rs.banka4.bank_service.exceptions.RequestFailed;
import rs.banka4.bank_service.exceptions.WrongTurn;
import rs.banka4.bank_service.repositories.AssetOwnershipRepository;
import rs.banka4.bank_service.repositories.OtcRequestRepository;
import rs.banka4.bank_service.repositories.StockRepository;
import rs.banka4.bank_service.tx.data.*;
import rs.banka4.bank_service.tx.data.OtcOffer;
import rs.banka4.bank_service.tx.data.PublicStock;
import rs.banka4.bank_service.tx.data.Seller;
import rs.banka4.bank_service.tx.data.StockDescription;
import rs.banka4.bank_service.tx.otc.config.InterbankRetrofitProvider;
import rs.banka4.bank_service.tx.otc.mapper.InterbankOtcMapper;
import rs.banka4.bank_service.tx.otc.service.InterbankOtcService;

@Service
@RequiredArgsConstructor
public class InterbankOtcServiceImpl implements InterbankOtcService {
    private final AssetOwnershipRepository assetOwnershipRepository;
    private final InterbankRetrofitProvider interbankRetrofit;
    private final StockRepository stockRepository;
    private final OtcRequestRepository otcRequestRepository;

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
    public void sendUpdateOtc(OtcOffer offer, ForeignBankId id) {
        final var otherBank =
            Stream.of(offer.buyerId(), offer.sellerId())
                .map(ForeignBankId::routingNumber)
                .filter(x -> !x.equals(ForeignBankId.OUR_ROUTING_NUMBER))
                .findFirst()
                .orElseThrow(
                    () -> new IllegalArgumentException("Calling sendUpdateOtc with a local offer")
                );
        try {
            var call =
                interbankRetrofit.get(otherBank)
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
            var call = interbankRetrofit.sendGetOtcNegotiation(id.routingNumber(), id.id());
            var response = call.execute();
            return response.body();
        } catch (IOException e) {
            throw new RequestFailed();
        }
    }
}
