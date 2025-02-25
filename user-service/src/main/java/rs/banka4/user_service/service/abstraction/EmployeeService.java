package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.*;

public interface EmployeeService {
    ResponseEntity<LoginResponseDto> login(LoginDto loginDto);
    ResponseEntity<RefreshTokenResponseDto> refreshToken(String token);
    ResponseEntity<MeResponseDto> getMe(String authorization);
    ResponseEntity<Void> logout(LogoutDto logoutDto);
    ResponseEntity<PrivilegesDto> getPrivileges();
    ResponseEntity<Void> createEmployee(CreateEmployeeDto dto);
    ResponseEntity<Page<EmployeeDto>> getAll(String firstName, String lastName, String email, String position, PageRequest pageRequest);
}
