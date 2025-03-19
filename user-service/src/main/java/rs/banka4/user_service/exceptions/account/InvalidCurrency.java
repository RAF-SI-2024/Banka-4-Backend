package rs.banka4.user_service.exceptions.account;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class InvalidCurrency extends BaseApiException {
    public InvalidCurrency(String currency) {
        super(HttpStatus.NOT_FOUND, Map.of("currency", currency));
    }
}
