package rs.banka4.rafeisen.common.exceptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
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
        return ResponseEntity.status(ex.getStatus())
            .body(formatErrorBody(ex.getClass(), ex.getExtra()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, ?>> handleValidationErrorResponse(
        MethodArgumentNotValidException ex
    ) {
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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(formatErrorBody("InvalidJsonInput", Map.of("message", ex.getMessage())));
    }

    /**
     * Raised when Spring cannot convert between types (e.g. we receive a poorly formed UUID or
     * such).
     */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<Map<String, ?>> handleTypeMismatchException(TypeMismatchException e) {
        return ResponseEntity.badRequest()
            .body(
                formatErrorBody(
                    "TypeMismatch",
                    Map.ofEntries(
                        Map.entry("errorCode", e.getErrorCode()),
                        Map.entry("propertyName", e.getPropertyName()),
                        Map.entry(
                            /* Nullable. lmao. */
                            "expectedType",
                            Optional.ofNullable(e.getRequiredType())
                                .map(Class::getSimpleName)
                                .orElse(null)
                        ),
                        Map.entry("badValue", e.getValue())
                    )
                )
            );
    }
}
