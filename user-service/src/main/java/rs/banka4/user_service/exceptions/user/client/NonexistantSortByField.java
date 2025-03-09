package rs.banka4.user_service.exceptions.user.client;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

import java.util.Map;

public class NonexistantSortByField extends BaseApiException {
    public NonexistantSortByField(String sortByField) { super(HttpStatus.UNPROCESSABLE_ENTITY, Map.of("sortByField", sortByField));}
}