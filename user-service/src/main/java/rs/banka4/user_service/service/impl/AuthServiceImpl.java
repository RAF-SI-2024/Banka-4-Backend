package rs.banka4.user_service.service.impl;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.config.RabbitMqConfig;
import rs.banka4.user_service.domain.auth.db.VerificationCode;
import rs.banka4.user_service.domain.auth.dtos.LogoutDto;
import rs.banka4.user_service.domain.auth.dtos.NotificationTransferDto;
import rs.banka4.user_service.domain.auth.dtos.RefreshTokenResponseDto;
import rs.banka4.user_service.domain.auth.dtos.UserVerificationRequestDto;
import rs.banka4.user_service.domain.user.User;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.exceptions.jwt.RefreshTokenRevoked;
import rs.banka4.user_service.exceptions.user.UserNotFound;
import rs.banka4.user_service.exceptions.user.VerificationCodeExpiredOrInvalid;
import rs.banka4.user_service.service.abstraction.AuthService;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.MessageHelper;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final VerificationCodeService verificationCodeService;
    private final RabbitTemplate rabbitTemplate;
    private final EmployeeService employeeService;
    private final ClientService clientService;

    @Override
    public void logout(LogoutDto logoutDto) {
        String refreshToken = logoutDto.refreshToken();
        jwtUtil.invalidateToken(refreshToken);
    }

    @Override
    public RefreshTokenResponseDto refreshToken(String token) {
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
            Employee employee = (Employee) findUserByEmail(username);
            newAccessToken = jwtUtil.generateToken(employee);
        } else {
            Client client = (Client) findUserByEmail(username);
            newAccessToken = jwtUtil.generateToken(client);
        }

        return new RefreshTokenResponseDto(newAccessToken);
    }

    @Override
    public void verifyAccount(UserVerificationRequestDto request) {
        VerificationCode verificationCode =
            verificationCodeService.validateVerificationCode(request.verificationCode())
                .orElseThrow(VerificationCodeExpiredOrInvalid::new);

        User user = findUserByEmail(verificationCode.getEmail());

        if (user instanceof Employee) {
            employeeService.activateEmployeeAccount((Employee) user, request.password());
        } else {
            clientService.activateClientAccount((Client) user, request.password());
        }
        verificationCodeService.markCodeAsUsed(verificationCode);
    }

    public User findUserByEmail(String email) {
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
    public void forgotPassword(String email) {
        VerificationCode verificationCode = verificationCodeService.createVerificationCode(email);

        User user = findUserByEmail(email);

        NotificationTransferDto message =
            MessageHelper.createForgotPasswordMessage(
                email,
                user.getFirstName(),
                verificationCode.getCode()
            );

        rabbitTemplate.convertAndSend(
            RabbitMqConfig.EXCHANGE_NAME,
            RabbitMqConfig.ROUTING_KEY,
            message
        );
    }
}
