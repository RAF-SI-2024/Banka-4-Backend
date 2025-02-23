package rs.banka4.user_service.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@Setter
public class BaseApiException extends RuntimeException {
    private final boolean failed = true;
    private final Class<? extends BaseApiException> code = getClass();
    private final Map<String, Object> extra;
    private final HttpStatus status;

    public BaseApiException(HttpStatus status, Map<String, Object> extra) {
        super();
        this.status = status;
        this.extra = extra;
    }
}
