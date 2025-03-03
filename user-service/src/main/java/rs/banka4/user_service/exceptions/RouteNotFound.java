package rs.banka4.user_service.exceptions;

import org.springframework.http.HttpStatus;

public class RouteNotFound  extends BaseApiException {
    public RouteNotFound() {
        super(HttpStatus.NOT_FOUND, null);
    }
}
