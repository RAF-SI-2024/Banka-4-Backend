package rs.banka4.bank_service.tx.data;

public record IdempotenceKey(
    int routingNumber,
    String locallyGeneratedKey
) {
}
