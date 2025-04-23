package rs.banka4.bank_service.tx.errors;

public class MessagePrepFailedException extends TxException {
    public MessagePrepFailedException(Throwable cause) {
        super("Failed to prepare a transaction for submission", cause);
    }
}
