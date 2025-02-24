package rs.banka4.user_service.service.abstraction;

import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.LoginDto;

public interface EmployeeService {
    ResponseEntity<?> login(LoginDto loginDto);
    ResponseEntity<?> refreshToken(String token);
    ResponseEntity<?> getMe(String authorization);
}
