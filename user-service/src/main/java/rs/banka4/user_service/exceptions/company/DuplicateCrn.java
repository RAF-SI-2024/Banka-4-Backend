package rs.banka4.user_service.exceptions.company;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class DuplicateCrn extends BaseApiException {
    public DuplicateCrn(String crn) {
        super(HttpStatus.CONFLICT, Map.of("crn", crn));
    }
}
