package rs.banka4.user_service.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ErrorResponseHandler {

    @ExceptionHandler(BaseApiException.class)
    public ResponseEntity<Map<String, Object>> handleErrorResponse(BaseApiException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("failed", true);
        response.put("code", ex.getClass().getName());

        if (ex.getExtra() != null) {
            response.put("extra", ex.getExtra());
        }

        return new ResponseEntity<>(response, ex.getStatus());
    }
}