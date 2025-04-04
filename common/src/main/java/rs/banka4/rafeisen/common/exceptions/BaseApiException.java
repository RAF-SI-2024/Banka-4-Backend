package rs.banka4.rafeisen.common.exceptions;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
@RequiredArgsConstructor
public class BaseApiException extends RuntimeException {
    private final HttpStatus status;
    private final Map<String, Object> extra;
}
