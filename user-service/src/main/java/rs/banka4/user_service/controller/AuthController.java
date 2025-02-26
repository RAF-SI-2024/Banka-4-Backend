package rs.banka4.user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.config.RabbitMqConfig;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.dto.requests.EmployeeVerificationRequestDto;
import rs.banka4.user_service.exceptions.NotFound;
import rs.banka4.user_service.exceptions.VerificationCodeExpiredOrInvalid;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.VerificationCode;
import rs.banka4.user_service.service.abstraction.AuthService;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.service.impl.VerificationCodeService;
import rs.banka4.user_service.utils.MessageHelper;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmployeeService employeeService;
    private final VerificationCodeService verificationCodeService;
    private final RabbitTemplate rabbitTemplate;

    @PostMapping("/employee/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginDto loginDto) {
        return employeeService.login(loginDto);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) {
        return authService.refreshToken(refreshTokenDto.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutDto logoutDto, @RequestHeader("Authorization") String authorization) {
        return authService.logout(logoutDto);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmployeeAccount(@RequestBody EmployeeVerificationRequestDto request) {
        Optional<VerificationCode> optionalVerificationCode = verificationCodeService.validateVerificationCode(request.verificationCode());
        if (optionalVerificationCode.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid or expired verification code.");
        }

        VerificationCode verificationCode = optionalVerificationCode.get();

        Optional<VerificationCode> savedVerificationCode = verificationCodeService.findByCode(verificationCode.getCode());
        if (savedVerificationCode.isEmpty()) {
            throw new VerificationCodeExpiredOrInvalid("Invalid or expired verification code.");
        }

        Optional<Employee> employee = employeeService.findEmployee(savedVerificationCode.get().getEmail());
        if (employee.isEmpty()) {
            throw new NotFound("Employee not found.");
        }

        employeeService.activateEmployeeAccount(employee.get(), request.password());

        NotificationTransferDto message = MessageHelper.createAccountActivationMessage(employee.get().getEmail(),
                employee.get().getFirstName(),
                verificationCode.getCode());

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_NAME,
                RabbitMqConfig.ROUTING_KEY,
                message
        );

        verificationCodeService.markCodeAsUsed(verificationCode);

        return ResponseEntity.ok("Account activated successfully.");
    }

    @GetMapping("/forgot-password/{email}")
    public ResponseEntity<?> forgotPassword(@PathVariable("email") String email) {
        VerificationCode verificationCode = verificationCodeService.createVerificationCode(email);

        Optional<Employee> employee = employeeService.findEmployee(email);
        if (employee.isEmpty()) {
            throw new NotFound("Employee not found.");
        }

        NotificationTransferDto message = MessageHelper.createForgotPasswordMessage(email,
                employee.get().getFirstName(),
                verificationCode.getCode());

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_NAME,
                RabbitMqConfig.ROUTING_KEY,
                message
        );

        return ResponseEntity.ok("Verification code sent to email.");
    }
}
