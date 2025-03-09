package rs.banka4.user_service.exceptions.company;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

import java.util.Map;

public class DuplicateCompanyName extends BaseApiException {
    public DuplicateCompanyName(String name) {
        super(HttpStatus.CONFLICT, Map.of("name", name));
    }
}
