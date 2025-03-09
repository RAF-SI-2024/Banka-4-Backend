package rs.banka4.user_service.exceptions.user.client;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

import java.util.Map;

public class ClientNotFound extends BaseApiException {
    public ClientNotFound(String email) {
        super(HttpStatus.NOT_FOUND,Map.of("email", email));
    }
}
