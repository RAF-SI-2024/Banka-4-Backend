package rs.banka4.user_service.exceptions.company;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class CompanyNotFound extends BaseApiException {

    public CompanyNotFound(String crn) {
        super(HttpStatus.NOT_FOUND, Map.of("crn", crn));
    }
}
