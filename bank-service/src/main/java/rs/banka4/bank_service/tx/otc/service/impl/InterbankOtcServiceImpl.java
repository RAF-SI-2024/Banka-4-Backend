package rs.banka4.bank_service.tx.otc.service.impl;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.banka4.bank_service.domain.assets.db.AssetOwnership;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.repositories.AssetOwnershipRepository;
import rs.banka4.bank_service.tx.data.PublicStock;
import rs.banka4.bank_service.tx.data.Seller;
import rs.banka4.bank_service.tx.data.StockDescription;
import rs.banka4.bank_service.tx.otc.service.InterbankOtcService;

@Service
@RequiredArgsConstructor
public class InterbankOtcServiceImpl implements InterbankOtcService {
    private final AssetOwnershipRepository assetOwnershipRepository;

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
        Set<String> tickers = new HashSet<>();
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
        return List.of();
    }
}
