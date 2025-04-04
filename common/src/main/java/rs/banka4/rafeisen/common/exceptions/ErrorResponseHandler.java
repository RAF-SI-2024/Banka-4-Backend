package rs.banka4.rafeisen.common.exceptions;

import java.util.HashMap;
import java.util.Map;
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
}
