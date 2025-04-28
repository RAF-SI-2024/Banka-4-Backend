package rs.banka4.rafeisen.common.exceptions;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class BaseApiException extends RuntimeException {
    private final HttpStatus status;
    private final Map<String, Object> extra;

    public BaseApiException(HttpStatus status, Map<String, Object> extra) {
        this(status, extra, null);
    }

    public BaseApiException(HttpStatus status, Map<String, Object> extra, Throwable cause) {
        super(cause);
        this.status = status;
        this.extra = extra;
    }
}
