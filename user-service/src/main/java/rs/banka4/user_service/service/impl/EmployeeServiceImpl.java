package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.LoginDto;
import rs.banka4.user_service.dto.LoginResponseDto;
import rs.banka4.user_service.dto.MeResponseDto;
import rs.banka4.user_service.dto.RefreshTokenResponseDto;
import rs.banka4.user_service.exceptions.IncorrectCredentials;
import rs.banka4.user_service.exceptions.NotActivated;
import rs.banka4.user_service.exceptions.NotAuthenticated;
import rs.banka4.user_service.exceptions.RefreshTokenExpired;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.utils.JwtUtil;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final EmployeeRepository employeeRepository;
    private final JwtUtil jwtUtil;

    @Override
    public ResponseEntity<LoginResponseDto> login(LoginDto loginDto) {
        try {
            CustomUserDetailsService.role = "employee";
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password()));
        } catch (Exception e) {
            throw new IncorrectCredentials();
        }

        Employee employee = employeeRepository.findByEmail(loginDto.email()).get();

        if (!employee.isEnabled()) throw new NotActivated();

        String accessToken = jwtUtil.generateToken(employee);
        String refreshToken = jwtUtil.generateRefreshToken(userDetailsService.loadUserByUsername(loginDto.email()));

        LoginResponseDto response = new LoginResponseDto(accessToken, refreshToken);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IncorrectCredentials();
        }
        String refreshToken = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(refreshToken);

        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new RefreshTokenExpired();
        }

        Employee employee = employeeRepository.findByEmail(username).orElseThrow(IncorrectCredentials::new);
        String newAccessToken = jwtUtil.generateToken(employee);

        RefreshTokenResponseDto response = new RefreshTokenResponseDto(newAccessToken);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<MeResponseDto> getMe(String authorization) {
        CustomUserDetailsService.role = "employee";
        String token = authorization.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        if (jwtUtil.isTokenExpired(token)) {
            throw new NotAuthenticated();
        }

        Employee employee = employeeRepository.findByEmail(username).orElseThrow(NotAuthenticated::new);

        MeResponseDto response = new MeResponseDto(employee.getId(), employee.getFirstName(), employee.getLastName());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> logout(String authorization) {
        String token = authorization.replace("Bearer ", "");
        jwtUtil.invalidateToken(token);

        return ResponseEntity.ok().build();
    }
}
