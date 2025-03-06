package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class CompanyNotFound extends BaseApiException{

    public CompanyNotFound(String crn) {
        super(HttpStatus.NOT_FOUND, Map.of("crn",crn));
    }
}
