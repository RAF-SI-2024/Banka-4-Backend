package rs.banka4.bank_service.tx.data;

public record Posting(
    TxAccount account,
    int amount,
    TxAsset asset
) {
}
