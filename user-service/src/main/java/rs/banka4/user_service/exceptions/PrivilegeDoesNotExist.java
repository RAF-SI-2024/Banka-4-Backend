package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.models.Privilege;

import java.util.Map;

public class PrivilegeDoesNotExist extends BaseApiException {
    public PrivilegeDoesNotExist(Privilege privilege) {
        super(HttpStatus.FORBIDDEN, Map.of("privilege", privilege));
    }
}
