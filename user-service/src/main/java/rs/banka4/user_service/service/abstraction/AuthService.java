package rs.banka4.user_service.service.abstraction;

import rs.banka4.user_service.domain.auth.dtos.LogoutDto;
import rs.banka4.user_service.domain.auth.dtos.RefreshTokenResponseDto;
import rs.banka4.user_service.domain.auth.dtos.UserVerificationRequestDto;

public interface AuthService {
    RefreshTokenResponseDto refreshToken(String token);
    void logout(LogoutDto logoutDto);
    void verifyAccount(UserVerificationRequestDto request);
    void forgotPassword(String email);
}
