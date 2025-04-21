package rs.banka4.bank_service.tx.data;

import rs.banka4.bank_service.domain.trading.db.ForeignBankId;

public record CommitTransaction(ForeignBankId transactionId) {
}
