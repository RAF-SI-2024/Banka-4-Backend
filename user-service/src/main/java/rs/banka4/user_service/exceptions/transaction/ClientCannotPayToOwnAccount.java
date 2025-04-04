package rs.banka4.user_service.exceptions.transaction;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class ClientCannotPayToOwnAccount extends BaseApiException {
    public ClientCannotPayToOwnAccount() {
        super(HttpStatus.CONFLICT, null);
    }
}
