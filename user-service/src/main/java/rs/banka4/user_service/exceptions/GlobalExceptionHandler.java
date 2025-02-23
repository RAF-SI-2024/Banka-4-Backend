package rs.banka4.user_service.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Map<String, Object>> handleBaseException(BaseException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("failed", ex.isFailed());
        response.put("code", ex.getCode().name());
        response.put("message", ex.getMessage());

        if (ex.getExtra() != null) {
            response.put("extra", ex.getExtra());
        }

        return new ResponseEntity<>(response, ex.getStatus());
    }
}