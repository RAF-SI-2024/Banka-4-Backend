package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.exceptions.*;
import rs.banka4.user_service.mapper.BasicEmployeeMapper;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.Privilege;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.specification.EmployeeSpecification;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

import java.util.List;
import java.util.Optional;
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
    private final BasicEmployeeMapper basicEmployeeMapper;
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

    public ResponseEntity<Void> createEmployee(CreateEmployeeDto dto) {
        if (employeeRepository.existsByEmail(dto.email())) {
            throw new DuplicateEmail(dto.email());
        }
        if (employeeRepository.existsByUsername(dto.username())) {
            throw new DuplicateUsername(dto.username());
        }
        Set<Privilege> validPrivileges = EnumSet.allOf(Privilege.class);
        dto.privilege().forEach(privilege -> {
            if (!validPrivileges.contains(privilege)) {
                throw new PrivilegeDoesNotExist(privilege);
            }
        });


        Employee employee = basicEmployeeMapper.toEntity(dto);
        employeeRepository.save(employee);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Page<EmployeeDto>> getAll(String firstName, String lastName, String email, String position, PageRequest pageRequest) {
        SpecificationCombinator<Employee> combinator = new SpecificationCombinator<>();

        if (firstName != null && !firstName.isEmpty()) {
            combinator.and(EmployeeSpecification.hasFirstName(firstName));
        }
        if (lastName != null && !lastName.isEmpty()) {
            combinator.and(EmployeeSpecification.hasLastName(lastName));
        }
        if (email != null && !email.isEmpty()) {
            combinator.and(EmployeeSpecification.hasEmail(email));
        }
        if (position != null && !position.isEmpty()) {
            combinator.and(EmployeeSpecification.hasPosition(position));
        }

        Page<Employee> employees = employeeRepository.findAll(combinator.build(), pageRequest);
        Page<EmployeeDto> dtos = employees.map(employee -> new EmployeeDto(
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
                employee.getDepartment()
        ));

        return ResponseEntity.ok(dtos);
    }

    @Override
    public Optional<Employee> findEmployee(String email) {
        return employeeRepository.findByEmail(email);
    }

    @Override
    public void activateEmployeeAccount(Employee employee, String password) {
        employee.setEnabled(true);
        employee.setPassword(passwordEncoder.encode(password));
        employeeRepository.save(employee);
    }

}
