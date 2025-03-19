package rs.banka4.user_service.exceptions.company;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class DuplicateCompanyName extends BaseApiException {
    public DuplicateCompanyName(String name) {
        super(HttpStatus.CONFLICT, Map.of("name", name));
    }
}
