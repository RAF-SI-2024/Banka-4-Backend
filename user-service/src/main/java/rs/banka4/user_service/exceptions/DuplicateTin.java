package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicateTin extends BaseApiException {
    public DuplicateTin(String tin) {
        super(HttpStatus.CONFLICT, Map.of("tin", tin));
    }
}
