package rs.banka4.rafeisen.common.exceptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rs.banka4.rafeisen.common.exceptions.jwt.ExpiredJwt;
import rs.banka4.rafeisen.common.exceptions.jwt.NoJwtProvided;
import rs.banka4.rafeisen.common.exceptions.jwt.Unauthorized;

@RestControllerAdvice
@Log4j2
public class ErrorResponseHandler {
    /**
     * Generate an error response body based on a class name. Uses the class simple name.
     *
     * @param errClass Class whose name represents the error code.
     * @param extra Extra error date. If null, field will be omitted.
     * @see Class#getSimpleName()
     */
    private Map<String, ?> formatErrorBody(Class<?> errClass, Map<String, ?> extra) {
        return formatErrorBody(errClass.getSimpleName(), extra);
    }

    /**
     * Generate an error response body.
     *
     * @param code Error code.
     * @param extra Extra error date. If null, field will be omitted.
     */
    private Map<String, ?> formatErrorBody(String code, Map<String, ?> extra) {
        final var result = new HashMap<String, Object>();
        result.put("failed", true);
        result.put("code", code);
        if (extra != null) result.put("extra", extra);
        return Collections.unmodifiableMap(result);
    }

    @ExceptionHandler(BaseApiException.class)
    public ResponseEntity<Map<String, ?>> handleErrorResponse(BaseApiException ex) {
        if (
            !(ex instanceof NoJwtProvided)
                && !(ex instanceof ExpiredJwt)
                && !(ex instanceof Unauthorized)
        ) log.debug("Reporting API error in API call", ex);
        return ResponseEntity.status(ex.getStatus())
            .body(formatErrorBody(ex.getClass(), ex.getExtra()));
    }

    @ExceptionHandler(NotImplementedException.class)
    public ResponseEntity<Map<String, ?>> handleNotImplementedResponse(NotImplementedException ex) {
        log.debug("TODO detected", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(formatErrorBody("NotImplemented", null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, ?>> handleValidationErrorResponse(
        MethodArgumentNotValidException ex
    ) {
        log.debug("Reporting argument validation error in API call", ex);
        Map<String, Object> errors = new HashMap<>();
        for (
            FieldError error : ex.getBindingResult()
                .getFieldErrors()
        ) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
            .body(formatErrorBody(ex.getClass(), errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, ?>> handleJsonParseError(HttpMessageNotReadableException ex) {
        log.debug("Reporting parse error in API call", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(formatErrorBody("InvalidJsonInput", Map.of("message", ex.getMessage())));
    }

    /**
     * Raised when Spring cannot convert between types (e.g. we receive a poorly formed UUID or
     * such).
     */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<Map<String, ?>> handleTypeMismatchException(TypeMismatchException e) {
        log.debug("Reporting type-mismatch error in API call", e);
        final var extraData = new HashMap<String, Object>();
        extraData.put("errorCode", e.getErrorCode());
        extraData.put("propertyName", e.getPropertyName());
        extraData.put(
            /* Nullable. lmao. */
            "expectedType",
            Optional.ofNullable(e.getRequiredType())
                .map(Class::getSimpleName)
                .orElse(null)
        );
        return ResponseEntity.badRequest()
            .body(formatErrorBody("TypeMismatch", extraData));
    }
}
