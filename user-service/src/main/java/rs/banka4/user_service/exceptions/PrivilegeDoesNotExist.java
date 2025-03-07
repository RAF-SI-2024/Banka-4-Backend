package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class PrivilegeDoesNotExist extends BaseApiException {
    public PrivilegeDoesNotExist(String privilege) {
        super(HttpStatus.BAD_REQUEST, Map.of("privilege", privilege));
    }
}
