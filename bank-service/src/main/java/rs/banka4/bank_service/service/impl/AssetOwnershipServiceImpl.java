package rs.banka4.bank_service.service.impl;


import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.assets.db.AssetOwnership;
import rs.banka4.bank_service.domain.assets.db.AssetOwnershipId;
import rs.banka4.bank_service.domain.assets.mappers.AssetMapper;
import rs.banka4.bank_service.domain.listing.dtos.SecurityType;
import rs.banka4.bank_service.domain.security.stock.db.Stock;
import rs.banka4.bank_service.domain.trading.dtos.PublicStocksDto;
import rs.banka4.bank_service.exceptions.AssetNotFound;
import rs.banka4.bank_service.exceptions.NotEnoughStock;
import rs.banka4.bank_service.exceptions.StockOwnershipNotFound;
import rs.banka4.bank_service.exceptions.user.UserNotFound;
import rs.banka4.bank_service.repositories.AssetOwnershipRepository;
import rs.banka4.bank_service.repositories.AssetRepository;
import rs.banka4.bank_service.repositories.StockRepository;
import rs.banka4.bank_service.repositories.UserRepository;
import rs.banka4.bank_service.service.abstraction.AssetOwnershipService;
import rs.banka4.bank_service.service.abstraction.ListingService;
import rs.banka4.bank_service.tx.data.PublicStock;
import rs.banka4.bank_service.tx.otc.service.InterbankOtcService;

@Service
@RequiredArgsConstructor
public class AssetOwnershipServiceImpl implements AssetOwnershipService {
    private final AssetOwnershipRepository assetOwnershipRepository;
    private static final Logger logger = LoggerFactory.getLogger(AssetOwnershipServiceImpl.class);
    private final ListingService listingService;
    private final StockRepository stockRepository;
    private final InterbankOtcService interbankOtcService;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AssetOwnership transferStock(
        @NonNull UUID userId,
        @NonNull UUID assetId,
        int amount,
        @NonNull TransferTo transferTo
    ) {
        Optional<AssetOwnership> assetOwnershipOptional =
            assetOwnershipRepository.findByMyId(userId, assetId);
        if (
            assetOwnershipOptional.isPresent()
                && assetOwnershipOptional.get()
                    .getId()
                    .getAsset() instanceof Stock
        ) {
            AssetOwnership assetOwnership = assetOwnershipOptional.get();
            if (transferTo == TransferTo.PUBLIC) {
                if (amount <= assetOwnership.getPrivateAmount()) {
                    // do the transfer from private to public
                    assetOwnership.setPrivateAmount(assetOwnership.getPrivateAmount() - amount);
                    assetOwnership.setPublicAmount(assetOwnership.getPublicAmount() + amount);
                    assetOwnershipRepository.save(assetOwnership);
                } else {
                    throw new NotEnoughStock();
                }
            } else {
                if (amount <= assetOwnership.getPublicAmount()) {
                    // do the transfer from public to private
                    assetOwnership.setPublicAmount(assetOwnership.getPublicAmount() - amount);
                    assetOwnership.setPrivateAmount(assetOwnership.getPrivateAmount() + amount);
                    assetOwnershipRepository.save(assetOwnership);
                } else {
                    throw new NotEnoughStock();
                }
            }
            return assetOwnership;
        } else {
            throw new StockOwnershipNotFound(assetId, userId);
        }
    }

    @Override
    public List<PublicStocksDto> getPublicStocks(Pageable pageable, String token) {
        final var allPublic =
            assetOwnershipRepository.findAllByPublicAmountGreaterThan(0, Limit.of(999));
        var otherBank = interbankOtcService.fetchPublicStocks();
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

    @Override
    public boolean changeAssetOwnership(
        UUID assetId,
        UUID userId,
        int privateAmount,
        int publicAmount,
        int reservedAmount
    ) {
        var ao = assetOwnershipRepository.findByMyId(userId, assetId);
        if (ao.isPresent()) {
            var assetOwnership = ao.get();
            if (
                assetOwnership.getPrivateAmount() + privateAmount >= 0
                    && assetOwnership.getPublicAmount() + publicAmount >= 0
                    && assetOwnership.getReservedAmount() + reservedAmount >= 0
            ) {
                assetOwnership.setPrivateAmount(assetOwnership.getPrivateAmount() + privateAmount);
                assetOwnership.setPublicAmount(assetOwnership.getPublicAmount() + publicAmount);
                assetOwnership.setReservedAmount(
                    assetOwnership.getReservedAmount() + reservedAmount
                );
                assetOwnershipRepository.save(assetOwnership);
                return true;
            }
            return false;
        } else {
            if (privateAmount < 0 || publicAmount < 0 || reservedAmount < 0) {
                return false;
            }
            var a = assetRepository.findById(assetId);
            if (a.isEmpty()) throw new AssetNotFound();
            var u = userRepository.findById(userId);
            if (u.isEmpty()) throw new UserNotFound(userId.toString());
            AssetOwnership assetOwnership =
                new AssetOwnership(
                    new AssetOwnershipId(u.get(), a.get()),
                    privateAmount,
                    publicAmount,
                    reservedAmount
                );
            assetOwnershipRepository.save(assetOwnership);
            return true;
        }
    }

    private List<PublicStocksDto> convertToPublicStocksDto(PublicStock dto) {
        List<PublicStocksDto> publicStocksDtos = new ArrayList<>();
        Stock stock =
            stockRepository.findByTicker(
                dto.stock()
                    .ticker()
            )
                .orElseThrow(AssetNotFound::new);
        MonetaryAmount latestPrice = listingService.getLatestPriceForStock(stock.getId());
        for (var x : dto.sellers()) {
            var userInfo = interbankOtcService.sendGetUserInfo(x.seller());
            publicStocksDtos.add(
                new PublicStocksDto(
                    SecurityType.STOCK,
                    x.seller(),
                    stock.getId(),
                    userInfo.displayName(),
                    stock.getTicker(),
                    stock.getName(),
                    x.amount(),
                    latestPrice,
                    OffsetDateTime.now()
                )
            );
        }
        return publicStocksDtos;
    }
}
