package rs.banka4.bank_service.tx.data;

import java.util.List;
import lombok.With;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;

@With
public record Transaction(
    List<Posting> postings,
    String message,
    ForeignBankId transactionId
) {
}
