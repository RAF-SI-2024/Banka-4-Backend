package rs.banka4.user_service.exceptions;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
@RequiredArgsConstructor
public class BaseApiException extends RuntimeException {
    private final HttpStatus status;
    private final Map<String, Object> extra;
}
