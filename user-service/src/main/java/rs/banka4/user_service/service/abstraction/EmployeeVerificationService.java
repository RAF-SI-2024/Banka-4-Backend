package rs.banka4.user_service.service.abstraction;

import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.requests.EmployeeVerificationRequestDto;

public interface EmployeeVerificationService {
    ResponseEntity<String> verifyEmployeeAccount(EmployeeVerificationRequestDto request);
    ResponseEntity<String> forgotPassword(String email);
}
