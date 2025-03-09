package rs.banka4.user_service.exceptions.company;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

import java.util.Map;

public class CompanyNotFound extends BaseApiException {

    public CompanyNotFound(String crn) {
        super(HttpStatus.NOT_FOUND, Map.of("crn",crn));
    }
}
