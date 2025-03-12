package rs.banka4.user_service.exceptions.user.client;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class NonexistantSortByField extends BaseApiException {
    public NonexistantSortByField(String sortByField) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, Map.of("sortByField", sortByField));
    }
}
