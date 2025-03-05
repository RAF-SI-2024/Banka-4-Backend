package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicateCompanyName extends BaseApiException {
    public DuplicateCompanyName(String name) {
        super(HttpStatus.CONFLICT, Map.of("name", name));
    }
}
