package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidCurrency extends BaseApiException{
    public InvalidCurrency(String currency) {
        super(HttpStatus.NOT_FOUND, Map.of("currency",currency));
    }


}
