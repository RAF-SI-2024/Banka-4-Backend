package rs.banka4.user_service.service.abstraction;

import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.LogoutDto;
import rs.banka4.user_service.dto.RefreshTokenResponseDto;

public interface AuthService {
    ResponseEntity<RefreshTokenResponseDto> refreshToken(String token);
    ResponseEntity<Void> logout(LogoutDto logoutDto);
}
