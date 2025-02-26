package rs.banka4.user_service.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.banka4.user_service.config.RabbitMqConfig;
import rs.banka4.user_service.dto.NotificationTransferDto;
import rs.banka4.user_service.dto.requests.EmployeeVerificationRequestDto;
import rs.banka4.user_service.exceptions.NotActivated;
import rs.banka4.user_service.exceptions.NotFound;
import rs.banka4.user_service.exceptions.VerificationCodeExpiredOrInvalid;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.VerificationCode;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.service.impl.VerificationCodeService;

import java.util.Optional;

@RestController
@RequestMapping("/auth/employee")
public class AuthEmployeeController {

    private final VerificationCodeService verificationCodeService;
    private final EmployeeService employeeService;
    private final RabbitTemplate rabbitTemplate;

    public AuthEmployeeController(VerificationCodeService verificationCodeService,
                                  EmployeeService employeeService,
                                  RabbitTemplate rabbitTemplate) {
        this.verificationCodeService = verificationCodeService;
        this.employeeService = employeeService;
        this.rabbitTemplate = rabbitTemplate;
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
