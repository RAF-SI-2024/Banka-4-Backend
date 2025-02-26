package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class VerificationCodeExpiredOrInvalid extends BaseApiException {
    public VerificationCodeExpiredOrInvalid(String message) {
        super(HttpStatus.FORBIDDEN, null);
    }
}
