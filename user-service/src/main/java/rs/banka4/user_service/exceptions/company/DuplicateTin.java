package rs.banka4.user_service.exceptions.company;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class DuplicateTin extends BaseApiException {
    public DuplicateTin(String tin) {
        super(HttpStatus.CONFLICT, Map.of("tin", tin));
    }
}
