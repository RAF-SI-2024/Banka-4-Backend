package rs.banka4.bank_service.tx.data;

import java.util.List;

public record PublicStock(
    StockDescription stock,
    List<Seller> sellers
) {
}
