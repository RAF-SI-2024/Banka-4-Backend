package rs.banka4.rafeisen.common.exceptions;

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

    @ExceptionHandler(BaseApiException.class)
    public ResponseEntity<Map<String, Object>> handleErrorResponse(BaseApiException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("failed", true);
        response.put(
            "code",
            ex.getClass()
                .getSimpleName()
        );

        if (ex.getExtra() != null) {
            response.put("extra", ex.getExtra());
        }

        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrorResponse(
        MethodArgumentNotValidException ex
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("failed", true);
        response.put("code", InvalidData.class.getSimpleName());

        Map<String, Object> errors = new HashMap<>();
        for (
            FieldError error : ex.getBindingResult()
                .getFieldErrors()
        ) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        response.put("extra", errors);
        return ResponseEntity.badRequest()
            .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonParseError(
        HttpMessageNotReadableException ex
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("failed", true);
        response.put("code", "InvalidJsonInput");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * Raised when Spring cannot convert between types (e.g. we receive a poorly formed UUID or
     * such).
     */
    @ExceptionHandler(TypeMismatchException.class)
    public Map<String, ?> handleTypeMismatchException(TypeMismatchException e) {
        return Map.ofEntries(
            Map.entry("failed", true),
            Map.entry("code", "TypeMismatch"),
            Map.entry(
                "extra",
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
