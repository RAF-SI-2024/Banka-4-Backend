package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.LogoutDto;
import rs.banka4.user_service.dto.RefreshTokenResponseDto;
import rs.banka4.user_service.exceptions.IncorrectCredentials;
import rs.banka4.user_service.exceptions.RefreshTokenExpired;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.abstraction.AuthService;
import rs.banka4.user_service.utils.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final EmployeeRepository employeeRepository;

    @Override
    public ResponseEntity<Void> logout(LogoutDto logoutDto) {
        String refreshToken = logoutDto.refreshToken();
        jwtUtil.invalidateToken(refreshToken);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(String token) {
        String username = jwtUtil.extractUsername(token);

        if (jwtUtil.isTokenInvalidated(token)) {
            throw new RefreshTokenExpired();
        }

        Employee employee = employeeRepository.findByEmail(username).orElseThrow(IncorrectCredentials::new);
        String newAccessToken = jwtUtil.generateToken(employee);

        RefreshTokenResponseDto response = new RefreshTokenResponseDto(newAccessToken);

        return ResponseEntity.ok(response);
    }
}
