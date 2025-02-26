package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ResourceNotFoundException extends BaseApiException{
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND,Map.of("error", message));
    }
}
