package rs.banka4.bank_service.service.impl;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.banka4.bank_service.domain.actuaries.db.ActuaryInfo;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.actuaries.db.dto.ActuaryPayloadDto;
import rs.banka4.bank_service.domain.auth.dtos.LoginDto;
import rs.banka4.bank_service.domain.auth.dtos.LoginResponseDto;
import rs.banka4.bank_service.domain.response.ActuaryInfoDto;
import rs.banka4.bank_service.domain.response.CombinedResponse;
import rs.banka4.bank_service.domain.response.LimitPayload;
import rs.banka4.bank_service.domain.user.PrivilegesDto;
import rs.banka4.bank_service.domain.user.User;
import rs.banka4.bank_service.domain.user.employee.db.Employee;
import rs.banka4.bank_service.domain.user.employee.dtos.CreateEmployeeDto;
import rs.banka4.bank_service.domain.user.employee.dtos.EmployeeDto;
import rs.banka4.bank_service.domain.user.employee.dtos.UpdateEmployeeDto;
import rs.banka4.bank_service.domain.user.employee.mapper.EmployeeMapper;
import rs.banka4.bank_service.exceptions.ActuaryNotFoundException;
import rs.banka4.bank_service.exceptions.CannotUpdateActuaryException;
import rs.banka4.bank_service.exceptions.NegativeLimitException;
import rs.banka4.bank_service.exceptions.user.DuplicateEmail;
import rs.banka4.bank_service.exceptions.user.DuplicateUsername;
import rs.banka4.bank_service.exceptions.user.IncorrectCredentials;
import rs.banka4.bank_service.exceptions.user.InvalidPhoneNumber;
import rs.banka4.bank_service.exceptions.user.NotAuthenticated;
import rs.banka4.bank_service.exceptions.user.UserNotFound;
import rs.banka4.bank_service.exceptions.user.client.NotActivated;
import rs.banka4.bank_service.repositories.ActuaryRepository;
import rs.banka4.bank_service.repositories.EmployeeRepository;
import rs.banka4.bank_service.repositories.UserRepository;
import rs.banka4.bank_service.security.PreAuthBankUserAuthentication;
import rs.banka4.bank_service.security.UnauthenticatedBankUserPrincipal;
import rs.banka4.bank_service.service.abstraction.EmployeeService;
import rs.banka4.bank_service.service.abstraction.JwtService;
import rs.banka4.bank_service.utils.specification.EmployeeSpecification;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.rafeisen.common.dto.EmployeeResponseDto;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserPrincipal;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.rafeisen.common.security.UserType;
import rs.banka4.rafeisen.common.utils.specification.SpecificationCombinator;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final ActuaryRepository actuaryRepository;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Override
    public LoginResponseDto login(LoginDto loginDto) {
        final var principal =
            new UnauthenticatedBankUserPrincipal(UserType.EMPLOYEE, loginDto.email());
        AuthenticatedBankUserAuthentication token;
        try {
            token =
                (AuthenticatedBankUserAuthentication) authenticationManager.authenticate(
                    new PreAuthBankUserAuthentication(principal, loginDto.password())
                );
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            LOGGER.debug("Login for {} failed", loginDto, e);
            throw new IncorrectCredentials();
        }

        Employee employee =
            employeeRepository.findByEmail(loginDto.email())
                .orElseThrow(() -> new UsernameNotFoundException(loginDto.email()));
        if (!employee.isActive() || employee.getPassword() == null) {
            LOGGER.debug("Login for {} failed: not activated", loginDto);
            throw new NotActivated();
        }

        String accessToken = jwtService.generateAccessToken(employee);
        String refreshToken = jwtService.generateRefreshToken(token.getPrincipal());

        return new LoginResponseDto(accessToken, refreshToken);
    }

    @Override
    public EmployeeResponseDto getMe(String authorization) {
        String token = authorization.replace("Bearer ", "");
        UUID employeeId = jwtService.extractUserId(token);

        if (jwtService.isTokenExpired(token)) throw new NotAuthenticated();

        return EmployeeMapper.INSTANCE.toResponseDto(
            employeeRepository.findById(employeeId)
                .orElseThrow(NotAuthenticated::new)
        );
    }

    @Override
    public ResponseEntity<PrivilegesDto> getPrivileges() {
        List<String> privileges =
            Stream.of(Privilege.values())
                .map(Privilege::name)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new PrivilegesDto(privileges));
    }

    @Override
    public void createEmployee(CreateEmployeeDto dto) {
        if (userService.existsByEmail(dto.email())) {
            throw new DuplicateEmail(dto.email());
        }

        if (!userService.isPhoneNumberValid(dto.phone())) {
            throw new InvalidPhoneNumber();
        }

        if (employeeRepository.existsByUsername(dto.username())) {
            throw new DuplicateUsername(dto.username());
        }
        Employee employee = EmployeeMapper.INSTANCE.toEntity(dto);
        employee.setPrivileges(dto.privilege());
        employeeRepository.save(employee);

        Employee admin = getLoggedInEmployee();
        if (
            !admin.getPrivileges()
                .contains(Privilege.ADMIN)
        ) return;

        ActuaryPayloadDto actuaryPayloadDto = null;
        if (
            dto.privilege()
                .contains(Privilege.SUPERVISOR)
        ) {
            actuaryPayloadDto = supervisorPayload(employee.getId());
        } else
            if (
                dto.privilege()
                    .contains(Privilege.AGENT)
            ) {
                actuaryPayloadDto = agentPayload(employee.getId());
            }

        if (actuaryPayloadDto != null) {
            createNewActuary(actuaryPayloadDto);
        }

        userService.sendVerificationEmail(employee.getFirstName(), employee.getEmail());
    }

    @Override
    public ResponseEntity<Page<EmployeeDto>> getAll(
        String firstName,
        String lastName,
        String email,
        String position,
        PageRequest pageRequest
    ) {
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
        Page<EmployeeDto> dtos =
            employees.map(
                employee -> new EmployeeDto(
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
                )
            );

        return ResponseEntity.ok(dtos);
    }

    @Override
    public Page<Employee> getAllActuaries(
        String firstName,
        String lastName,
        String email,
        String position,
        PageRequest pageRequest
    ) {
        Employee loggedIn = getLoggedInEmployee();
        Set<Privilege> privileges = loggedIn.getPrivileges();

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

        // Admin sees agents and supervisors, supervisor only sees agents
        if (privileges.contains(Privilege.ADMIN)) {
            combinator.and(
                Specification.where(EmployeeSpecification.hasPrivilege(Privilege.SUPERVISOR))
                    .or(EmployeeSpecification.hasPrivilege(Privilege.AGENT))
            );
        } else if (privileges.contains(Privilege.SUPERVISOR)) {
            combinator.and(EmployeeSpecification.hasPrivilege(Privilege.AGENT));
        }

        return employeeRepository.findAll(combinator.build(), pageRequest);
    }

    @Override
    public Optional<Employee> findEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    @Override
    public Optional<Employee> findEmployeeById(UUID id) {
        return employeeRepository.findById(id);
    }

    @Override
    public void activateEmployeeAccount(Employee employee, String password) {
        employee.setEnabled(true);
        employee.setActive(true);
        employee.setPassword(passwordEncoder.encode(password));
        employeeRepository.save(employee);
    }

    @Override
    public void updateEmployee(UUID id, UpdateEmployeeDto updateEmployeeDto) {
        Employee employee =
            employeeRepository.findById(id)
                .orElseThrow(() -> new UserNotFound(id.toString()));

        Set<Privilege> oldPrivileges = employee.getPrivileges();

        if (userService.existsByEmail(updateEmployeeDto.email())) {
            throw new DuplicateEmail(updateEmployeeDto.email());
        }

        if (
            updateEmployeeDto.phoneNumber() != null
                && !userService.isPhoneNumberValid(updateEmployeeDto.phoneNumber())
        ) {
            throw new InvalidPhoneNumber();
        }

        if (employeeRepository.existsByUsername(updateEmployeeDto.username())) {
            throw new DuplicateUsername(updateEmployeeDto.username());
        }

        EmployeeMapper.INSTANCE.fromUpdate(employee, updateEmployeeDto);
        if (updateEmployeeDto.privilege() != null) {
            employee.setPrivileges(updateEmployeeDto.privilege());
        }
        employeeRepository.save(employee);

        Employee admin = getLoggedInEmployee();
        if (
            updateEmployeeDto.privilege() == null
                || updateEmployeeDto.privilege()
                    .isEmpty()
                || !admin.getPrivileges()
                    .contains(Privilege.ADMIN)
        ) return;

        ActuaryPayloadDto actuaryPayloadDto = null;

        // Employee -> Actuator
        if (
            !oldPrivileges.contains(Privilege.SUPERVISOR)
                && !oldPrivileges.contains(Privilege.AGENT)
        ) {
            if (
                updateEmployeeDto.privilege()
                    .contains(Privilege.SUPERVISOR)
            ) {
                actuaryPayloadDto = supervisorPayload(employee.getId());
            } else
                if (
                    updateEmployeeDto.privilege()
                        .contains(Privilege.AGENT)
                ) {
                    actuaryPayloadDto = agentPayload(employee.getId());
                }

            if (actuaryPayloadDto != null) {
                createNewActuary(actuaryPayloadDto);
            }
        }
        // Agent <-> Supervisor or Actuary -> Employee
        else {
            UUID pathId = employee.getId();
            if (
                updateEmployeeDto.privilege()
                    .contains(Privilege.SUPERVISOR)
            ) {
                actuaryPayloadDto = supervisorPayload(employee.getId());
            } else
                if (
                    updateEmployeeDto.privilege()
                        .contains(Privilege.AGENT)
                ) {
                    actuaryPayloadDto = agentPayload(employee.getId());
                } else {
                    pathId = admin.getId();
                    // only id matters
                    actuaryPayloadDto = agentPayload(employee.getId());
                }

            changeActuaryDetails(pathId, actuaryPayloadDto);
        }
    }

    @Override
    public EmployeeResponseDto getEmployeeById(UUID id) {
        Employee employee =
            employeeRepository.findById(id)
                .orElseThrow(() -> new UserNotFound(id.toString()));
        return EmployeeMapper.INSTANCE.toResponseDto(employee);
    }

    private Employee getLoggedInEmployee() {
        Authentication auth =
            SecurityContextHolder.getContext()
                .getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new NotAuthenticated();
        }
        AuthenticatedBankUserPrincipal principal =
            (AuthenticatedBankUserPrincipal) auth.getPrincipal();
        return employeeRepository.findById(principal.userId())
            .orElseThrow(NotAuthenticated::new);
    }

    private ActuaryPayloadDto agentPayload(UUID id) {
        return new ActuaryPayloadDto(true, BigDecimal.valueOf(10000), CurrencyCode.RSD, id);
    }

    private ActuaryPayloadDto supervisorPayload(UUID id) {
        return new ActuaryPayloadDto(false, null, CurrencyCode.RSD, id);
    }

    private void createNewActuary(ActuaryPayloadDto dto) {
        if (
            dto.limitAmount() != null
                && dto.limitAmount()
                    .compareTo(BigDecimal.ZERO)
                    < 0
        ) {
            throw new NegativeLimitException(
                dto.actuaryId()
                    .toString()
            );
        }
        Optional<User> user = userRepository.findById(dto.actuaryId());
        if (user.isEmpty()) {
            throw new UserNotFound(
                dto.actuaryId()
                    .toString()
            );
        }
        ActuaryInfo actuaryInfo = new ActuaryInfo();
        actuaryInfo.setUserId(
            user.get()
                .getId()
        );
        actuaryInfo.setLimit(new MonetaryAmount(dto.limitAmount(), CurrencyCode.RSD));
        actuaryInfo.setUsedLimit(new MonetaryAmount(BigDecimal.ZERO, CurrencyCode.RSD));
        actuaryInfo.setNeedApproval(dto.needsApproval());
        actuaryRepository.save(actuaryInfo);
    }

    @Override
    public void changeActuaryDetails(UUID actuaryId, ActuaryPayloadDto dto) {

        if (!actuaryId.equals(dto.actuaryId())) {
            // case when we send the admin id as the path parameter also when all the securities
            // should be transfered to the admin actuaryId is admins id
        }


        if (
            dto.limitAmount() != null
                && dto.limitAmount()
                    .compareTo(BigDecimal.ZERO)
                    < 0
        ) {
            throw new NegativeLimitException(actuaryId.toString());
        }


        ActuaryInfo actuaryInfo =
            actuaryRepository.findById(dto.actuaryId())
                .orElseThrow(() -> new ActuaryNotFoundException(dto.actuaryId()));

        actuaryInfo.setNeedApproval(dto.needsApproval());
        actuaryInfo.setLimit(new MonetaryAmount(dto.limitAmount(), CurrencyCode.RSD));
        actuaryRepository.save(actuaryInfo);
    }

    @Override
    public Page<CombinedResponse> search(
        AuthenticatedBankUserAuthentication auth,
        String firstName,
        String lastName,
        String email,
        String position,
        int page,
        int size
    ) {
        return getAllActuaries(
            firstName,
            lastName,
            email,
            position,
            PageRequest.of(page, size, Sort.by("id"))
        ).map(this::toCombinedResponse);
    }

    private CombinedResponse toCombinedResponse(Employee employee) {
        return actuaryRepository.findById(employee.getId())
            .map(actuaryInfo -> {
                ActuaryInfoDto dto =
                    new ActuaryInfoDto(
                        actuaryInfo.isNeedApproval(),
                        actuaryInfo.getLimit()
                            .getAmount(),
                        actuaryInfo.getUsedLimit() != null
                            ? actuaryInfo.getUsedLimit()
                                .getAmount()
                            : null,
                        actuaryInfo.getLimit()
                            .getCurrency()
                    );
                return new CombinedResponse(EmployeeMapper.INSTANCE.toResponseDto(employee), dto);
            })
            .orElseThrow(
                () -> new IllegalStateException(
                    "Missing ActuaryInfo for employee %s".formatted(employee)
                )
            );
    }

    @Override
    public void updateLimit(UUID actuaryId, LimitPayload dto) {
        ActuaryInfo actuaryInfo =
            actuaryRepository.findById(actuaryId)
                .orElseThrow(() -> new ActuaryNotFoundException(actuaryId));

        // Supervisors limit cannot be changed
        if (!actuaryInfo.isNeedApproval()) {
            throw new CannotUpdateActuaryException(actuaryId.toString());
        }
        if (
            dto.limitAmount()
                .compareTo(BigDecimal.ZERO)
                < 0
        ) {
            throw new NegativeLimitException(actuaryId.toString());
        }

        actuaryInfo.setLimit(new MonetaryAmount(dto.limitAmount(), dto.limitCurrencyCode()));
        actuaryRepository.save(actuaryInfo);
    }

    @Override
    public void resetUsedLimit(UUID actuaryId) {
        ActuaryInfo actuaryInfo =
            actuaryRepository.findById(actuaryId)
                .orElseThrow(() -> new ActuaryNotFoundException(actuaryId));
        actuaryInfo.setUsedLimit(
            resetLimit(
                actuaryInfo.getUsedLimit()
                    .getCurrency()
            )
        );
        actuaryRepository.save(actuaryInfo);
    }

    public static MonetaryAmount resetLimit(CurrencyCode currencyCode) {
        MonetaryAmount monetaryAmount = new MonetaryAmount();
        monetaryAmount.setAmount(BigDecimal.valueOf(0));
        monetaryAmount.setCurrency(currencyCode);
        return monetaryAmount;
    }
}
