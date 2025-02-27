package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.config.RabbitMqConfig;
import rs.banka4.user_service.dto.LogoutDto;
import rs.banka4.user_service.dto.NotificationTransferDto;
import rs.banka4.user_service.dto.RefreshTokenResponseDto;
import rs.banka4.user_service.dto.requests.EmployeeVerificationRequestDto;
import rs.banka4.user_service.exceptions.IncorrectCredentials;
import rs.banka4.user_service.exceptions.NotFound;
import rs.banka4.user_service.exceptions.VerificationCodeExpiredOrInvalid;
import rs.banka4.user_service.exceptions.jwt.RefreshTokenRevoked;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.VerificationCode;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.abstraction.AuthService;
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
            throw new RefreshTokenRevoked();
        }

        Employee employee = employeeRepository.findByEmail(username).orElseThrow(IncorrectCredentials::new);
        String newAccessToken = jwtUtil.generateToken(employee);

        RefreshTokenResponseDto response = new RefreshTokenResponseDto(newAccessToken);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> verifyAccount(EmployeeVerificationRequestDto request) {
        VerificationCode verificationCode = verificationCodeService.validateVerificationCode(request.verificationCode())
                .orElseThrow(VerificationCodeExpiredOrInvalid::new);

        Employee employee = employeeService.findEmployeeByEmail(verificationCode.getEmail())
                .orElseThrow(NotFound::new);

        employeeService.activateEmployeeAccount(employee, request.password());
        verificationCodeService.markCodeAsUsed(verificationCode);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> forgotPassword(String email) {
        VerificationCode verificationCode = verificationCodeService.createVerificationCode(email);

        Optional<Employee> employee = employeeService.findEmployeeByEmail(email);
        if (employee.isEmpty()) {
            throw new NotFound();
        }

        NotificationTransferDto message = MessageHelper.createForgotPasswordMessage(email,
                employee.get().getFirstName(),
                verificationCode.getCode());

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_NAME,
                RabbitMqConfig.ROUTING_KEY,
                message
        );

        return ResponseEntity.ok().build();
    }

}
