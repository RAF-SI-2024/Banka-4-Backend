package rs.banka4.user_service.exceptions.transaction;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class ClientCannotTransferToSameAccount extends BaseApiException {
    public ClientCannotTransferToSameAccount() {
        super(HttpStatus.CONFLICT, null);
    }
}
