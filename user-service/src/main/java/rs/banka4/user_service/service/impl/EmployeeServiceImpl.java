package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.auth.dtos.LoginDto;
import rs.banka4.user_service.domain.auth.dtos.LoginResponseDto;
import rs.banka4.user_service.domain.user.PrivilegesDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeResponseDto;
import rs.banka4.user_service.domain.user.employee.dtos.CreateEmployeeDto;
import rs.banka4.user_service.domain.user.employee.dtos.UpdateEmployeeDto;
import rs.banka4.user_service.exceptions.*;
import rs.banka4.user_service.domain.user.employee.mapper.EmployeeMapper;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.domain.user.Privilege;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.specification.EmployeeSpecification;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final EmployeeRepository employeeRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Override
    public ResponseEntity<LoginResponseDto> login(LoginDto loginDto) {
        CustomUserDetailsService.role = "employee"; // Consider refactoring this into a more robust role management system

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password()));
        } catch (BadCredentialsException e) {
            throw new IncorrectCredentials();
        }

        Employee employee = employeeRepository.findByEmail(loginDto.email()).orElseThrow(() -> new UsernameNotFoundException(loginDto.email()));
        if (!employee.isActive() || employee.getPassword() == null) {
            throw new NotActivated();
        }

        String accessToken = jwtUtil.generateToken(employee);
        String refreshToken = jwtUtil.generateRefreshToken(userDetailsService.loadUserByUsername(loginDto.email()), "employee");

        return ResponseEntity.ok(new LoginResponseDto(accessToken, refreshToken));
    }

    @Override
    public ResponseEntity<EmployeeResponseDto> getMe(String authorization) {
        String token = authorization.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        if (jwtUtil.isTokenExpired(token)) {
            throw new NotAuthenticated();
        }

        Employee employee = employeeRepository.findByEmail(username).orElseThrow(NotAuthenticated::new);

        EmployeeResponseDto response = new EmployeeResponseDto(
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
                employee.getPrivileges(),
                employee.isActive()
        );

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PrivilegesDto> getPrivileges() {
        List<String> privileges = Stream.of(Privilege.values())
                .map(Privilege::name)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new PrivilegesDto(privileges));
    }

    public void createEmployee(CreateEmployeeDto dto) {
        if (userService.existsByEmail(dto.email())) {
            throw new DuplicateEmail(dto.email());
        }
        if (employeeRepository.existsByUsername(dto.username())) {
            throw new DuplicateUsername(dto.username());
        }
        Employee employee = EmployeeMapper.INSTANCE.toEntity(dto);
        employeeRepository.save(employee);

        userService.sendVerificationEmail(employee.getFirstName(), employee.getEmail());
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
                employee.getDepartment(),
                employee.isActive()
        ));

        return ResponseEntity.ok(dtos);
    }

    public Optional<Employee> findEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    @Override
    public void activateEmployeeAccount(Employee employee, String password) {
        employee.setEnabled(true);
        employee.setActive(true);
        employee.setPassword(passwordEncoder.encode(password));
        employeeRepository.save(employee);
    }

    public void updateEmployee(UUID id, UpdateEmployeeDto updateEmployeeDto) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new UserNotFound(id.toString()));

        if (userService.existsByEmail(updateEmployeeDto.email())) {
            throw new DuplicateEmail(updateEmployeeDto.email());
        }
        if (employeeRepository.existsByUsername(updateEmployeeDto.username())) {
            throw new DuplicateUsername(updateEmployeeDto.username());
        }

        EmployeeMapper.INSTANCE.fromUpdate(employee, updateEmployeeDto);
        employeeRepository.save(employee);
    }

    @Override
    public ResponseEntity<EmployeeResponseDto> getEmployee(String id) {
//        var employee = employeeRepository.findById(id).orElseThrow(() -> new UserNotFound(id));
//
//        EmployeeResponseDto response = new EmployeeResponseDto(
//                employee.getId(),
//                employee.getFirstName(),
//                employee.getLastName(),
//                employee.getDateOfBirth(),
//                employee.getGender(),
//                employee.getEmail(),
//                employee.getPhone(),
//                employee.getAddress(),
//                employee.getUsername(),
//                employee.getPosition(),
//                employee.getDepartment(),
//                employee.getPrivileges(),
//                employee.isActive()
//        );

//        return ResponseEntity.ok(response);
        return null;
    }

}
