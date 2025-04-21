package rs.banka4.bank_service.tx.data;

import java.util.List;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;

public record Transaction(
    List<Posting> postings,
    String message,
    ForeignBankId transactionId
) {
}
