package rs.banka4.user_service.exceptions.user;

import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class VerificationCodeExpiredOrInvalid extends BaseApiException {
    public VerificationCodeExpiredOrInvalid() {
        super(HttpStatus.FORBIDDEN, null);
    }
}
