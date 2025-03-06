package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class NonexistantSortByField extends BaseApiException {
    public NonexistantSortByField(String sortByField) { super(HttpStatus.BAD_REQUEST, Map.of("sortByField", sortByField));}
}