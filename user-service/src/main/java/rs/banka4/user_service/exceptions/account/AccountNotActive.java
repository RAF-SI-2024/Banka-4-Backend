package rs.banka4.user_service.exceptions.account;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class AccountNotActive extends BaseApiException {
    public AccountNotActive() {
        super(HttpStatus.FORBIDDEN, Map.of("active", false));
    }
}
