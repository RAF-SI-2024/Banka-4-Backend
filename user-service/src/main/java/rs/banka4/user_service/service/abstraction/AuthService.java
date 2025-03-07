package rs.banka4.user_service.service.abstraction;

import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.auth.dtos.LogoutDto;
import rs.banka4.user_service.domain.auth.dtos.RefreshTokenResponseDto;
import rs.banka4.user_service.domain.auth.dtos.UserVerificationRequestDto;

public interface AuthService {
    ResponseEntity<RefreshTokenResponseDto> refreshToken(String token);
    ResponseEntity<Void> logout(LogoutDto logoutDto);
    ResponseEntity<Void> verifyAccount(UserVerificationRequestDto request);
    ResponseEntity<Void> forgotPassword(String email);
}
