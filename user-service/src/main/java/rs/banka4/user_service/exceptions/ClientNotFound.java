package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class ClientNotFound extends BaseApiException{
    public ClientNotFound(UUID clientId) {
        super(HttpStatus.NOT_FOUND,Map.of("clientId",clientId));
    }
}
