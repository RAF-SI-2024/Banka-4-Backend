package rs.banka4.user_service.service.abstraction;

import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.LoginDto;
import rs.banka4.user_service.dto.LoginResponseDto;
import rs.banka4.user_service.dto.MeResponseDto;
import rs.banka4.user_service.dto.RefreshTokenResponseDto;

public interface EmployeeService {
    ResponseEntity<LoginResponseDto> login(LoginDto loginDto);
    ResponseEntity<RefreshTokenResponseDto> refreshToken(String token);
    ResponseEntity<MeResponseDto> getMe(String authorization);
    ResponseEntity<Void> logout(String authorization);
}
