package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.exceptions.*;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.Privilege;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.utils.JwtUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final EmployeeRepository employeeRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

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
        String username = jwtUtil.extractUsername(token);

        if (jwtUtil.isTokenInvalidated(token)) {
            throw new RefreshTokenExpired();
        }

        Employee employee = employeeRepository.findByEmail(username).orElseThrow(IncorrectCredentials::new);
        String newAccessToken = jwtUtil.generateToken(employee);

        RefreshTokenResponseDto response = new RefreshTokenResponseDto(newAccessToken);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<MeResponseDto> getMe(String authorization) {
        String token = authorization.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        if (jwtUtil.isTokenExpired(token)) {
            throw new NotAuthenticated();
        }

        Employee employee = employeeRepository.findByEmail(username).orElseThrow(NotAuthenticated::new);

        MeResponseDto response = new MeResponseDto(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getDateOfBirth(),
                employee.getGender(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getAddress(),
                employee.getUsername(),
                employee.getPosition(),
                employee.getDepartment(),
                employee.getPrivileges()
        );

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> logout(LogoutDto logoutDto) {
        String refreshToken = logoutDto.refreshToken();
        jwtUtil.invalidateToken(refreshToken);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<PrivilegesDto> getPrivileges() {
        List<String> privileges = Stream.of(Privilege.values())
                .map(Privilege::name)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new PrivilegesDto(privileges));
    }

    public ResponseEntity<CreateEmployeeResponse> createEmployee(CreateEmployeeDto dto) {
        if(employeeRepository.existsByEmail(dto.email())) {
            throw new DuplicateEmail(dto.email());
        }
        if(employeeRepository.existsByUsername(dto.username())) {
            throw new DuplicateUsername(dto.username());
        }
        Set<Privilege> validPrivileges = EnumSet.allOf(Privilege.class);
        dto.privilege().forEach(privilege -> {
            if (!validPrivileges.contains(privilege)) {
                throw new PrivilegeDoesNotExist(privilege);
            }
        });




        Employee employee = new Employee();
        employee.setFirstName(dto.firstName());
        employee.setLastName(dto.lastName());
        employee.setUsername(dto.username());
        employee.setDateOfBirth(dto.dateOfBirth());
        employee.setGender(dto.gender());
        employee.setEmail(dto.email());
        employee.setPhone(dto.phone());
        employee.setAddress(dto.address());
        //should be encoded i guess
        employee.setPassword(passwordEncoder.encode(dto.password()));
        employee.setPrivileges(dto.privilege());
        employee.setPosition(dto.position());
        employee.setPhone(dto.phone());
        employee.setDepartment(dto.department());
        //he should not enabled right away
        employee.setEnabled(false);

        employeeRepository.save(employee);



        return ResponseEntity.ok(new CreateEmployeeResponse(dto.username(), dto.email()));
    }
}
