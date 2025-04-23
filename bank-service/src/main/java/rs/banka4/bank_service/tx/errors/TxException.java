package rs.banka4.bank_service.tx.errors;

public class TxException extends RuntimeException {
    public TxException(String message, Throwable cause) {
        super(message, cause);
    }

    public TxException(String message) {
        super(message);
    }
}
