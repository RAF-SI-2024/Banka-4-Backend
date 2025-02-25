package rs.banka4.user_service.service.abstraction;

import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.*;

public interface EmployeeService {
    ResponseEntity<LoginResponseDto> login(LoginDto loginDto);
    ResponseEntity<RefreshTokenResponseDto> refreshToken(String token);
    ResponseEntity<MeResponseDto> getMe(String authorization);
    ResponseEntity<Void> logout(LogoutDto logoutDto);
    ResponseEntity<PrivilegesDto> getPrivileges();
    ResponseEntity<CreateEmployeeResponse> createEmployee(CreateEmployeeDto dto);
}
