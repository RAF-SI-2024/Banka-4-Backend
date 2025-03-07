package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.config.RabbitMqConfig;
import rs.banka4.user_service.domain.auth.dtos.LogoutDto;
import rs.banka4.user_service.domain.auth.dtos.NotificationTransferDto;
import rs.banka4.user_service.domain.auth.dtos.RefreshTokenResponseDto;
import rs.banka4.user_service.domain.auth.dtos.UserVerificationRequestDto;
import rs.banka4.user_service.exceptions.IncorrectCredentials;
import rs.banka4.user_service.exceptions.NotFound;
import rs.banka4.user_service.exceptions.UserNotFound;
import rs.banka4.user_service.exceptions.VerificationCodeExpiredOrInvalid;
import rs.banka4.user_service.exceptions.jwt.RefreshTokenRevoked;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.domain.user.User;
import rs.banka4.user_service.domain.auth.db.VerificationCode;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.abstraction.AuthService;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.MessageHelper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final EmployeeRepository employeeRepository;
    private final VerificationCodeService verificationCodeService;
    private final RabbitTemplate rabbitTemplate;
    private final EmployeeService employeeService;
    private final ClientService clientService;
    private final ClientRepository clientRepository;

    @Override
    public ResponseEntity<Void> logout(LogoutDto logoutDto) {
        String refreshToken = logoutDto.refreshToken();
        jwtUtil.invalidateToken(refreshToken);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(String token) {
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        if (jwtUtil.isTokenInvalidated(token)) {
            throw new RefreshTokenRevoked();
        }

        String newAccessToken;

        if (role.isEmpty()) {
            throw new IllegalArgumentException();
        }

        if (role.equals("employee")) {
            Employee employee = employeeRepository.findByEmail(username).orElseThrow(IncorrectCredentials::new);
            newAccessToken = jwtUtil.generateToken(employee);
        } else {
            Client client = clientRepository.findByEmail(username).orElseThrow(NotFound::new);
            newAccessToken = jwtUtil.generateToken(client);
        }

        RefreshTokenResponseDto response = new RefreshTokenResponseDto(newAccessToken);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> verifyAccount(UserVerificationRequestDto request) {
        VerificationCode verificationCode = verificationCodeService.validateVerificationCode(request.verificationCode())
                .orElseThrow(VerificationCodeExpiredOrInvalid::new);

        User user = findUserByEmail(verificationCode.getEmail());

        if (user instanceof Employee) {
            employeeService.activateEmployeeAccount((Employee) user, request.password());
        } else {
            clientService.activateClientAccount((Client) user, request.password());
        }
        verificationCodeService.markCodeAsUsed(verificationCode);

        return ResponseEntity.ok().build();
    }

    private User findUserByEmail(String email) {
        Optional<Employee> employee = employeeService.findEmployeeByEmail(email);
        if (employee.isPresent()) {
            return employee.get();
        }

        Optional<Client> client = clientService.getClientByEmail(email);
        if (client.isPresent()) {
            return client.get();
        }

        throw new UserNotFound(email);
    }

    @Override
    public ResponseEntity<Void> forgotPassword(String email) {
        VerificationCode verificationCode = verificationCodeService.createVerificationCode(email);

        User user = findUserByEmail(email);

        NotificationTransferDto message = MessageHelper.createForgotPasswordMessage(email,
                user.getFirstName(),
                verificationCode.getCode());

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_NAME,
                RabbitMqConfig.ROUTING_KEY,
                message
        );

        return ResponseEntity.ok().build();
    }

}
