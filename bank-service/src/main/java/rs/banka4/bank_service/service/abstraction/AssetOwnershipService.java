package rs.banka4.bank_service.service.abstraction;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import rs.banka4.bank_service.domain.assets.db.AssetOwnership;
import rs.banka4.bank_service.domain.options.db.Asset;
import rs.banka4.bank_service.domain.trading.dtos.PublicStocksDto;
import rs.banka4.bank_service.domain.user.User;
import rs.banka4.bank_service.service.impl.TransferTo;

public interface AssetOwnershipService {
    AssetOwnership transferStock(UUID userId, UUID assetId, int amount, TransferTo transferTo);

    List<PublicStocksDto> getPublicStocks(Pageable pageable, String token);

    boolean changeAssetOwnership(
        UUID assetId,
        UUID userId,
        int privateAmount,
        int publicAmount,
        int reservedAmount
    );

    default boolean changeAssetOwnership(
        Asset asset,
        User user,
        int privateAmount,
        int publicAmount,
        int reservedAmount
    ) {
        return changeAssetOwnership(
            asset.getId(),
            user.getId(),
            privateAmount,
            publicAmount,
            reservedAmount
        );
    }
}
