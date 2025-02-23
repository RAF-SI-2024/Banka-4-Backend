package rs.banka4.user_service.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@Setter
public class BaseException extends RuntimeException {
    private final boolean failed;
    private final ErrorCode code;
    private final Map<String, Object> extra;
    private final HttpStatus status;

    public BaseException(ErrorCode code, String message, Map<String, Object> extra, HttpStatus status) {
        super(message);
        this.failed = true;
        this.code = code;
        this.extra = extra;
        this.status = status;
    }
}
