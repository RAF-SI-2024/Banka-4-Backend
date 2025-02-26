package rs.banka4.user_service.service.abstraction;

import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.LogoutDto;
import rs.banka4.user_service.dto.RefreshTokenResponseDto;
import rs.banka4.user_service.dto.requests.EmployeeVerificationRequestDto;

public interface AuthService {
    ResponseEntity<RefreshTokenResponseDto> refreshToken(String token);
    ResponseEntity<Void> logout(LogoutDto logoutDto);
    ResponseEntity<Void> verifyAccount(EmployeeVerificationRequestDto request);
    ResponseEntity<Void> forgotPassword(String email);
}
