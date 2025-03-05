package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicateCrn extends BaseApiException {
    public DuplicateCrn(String crn) {
        super(HttpStatus.CONFLICT, Map.of("crn", crn));
    }
}
