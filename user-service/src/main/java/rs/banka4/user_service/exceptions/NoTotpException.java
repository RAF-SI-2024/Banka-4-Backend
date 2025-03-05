package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class NoTotpException extends BaseApiException {
  public NoTotpException() {
    super(HttpStatus.BAD_REQUEST, null);
  }
}
