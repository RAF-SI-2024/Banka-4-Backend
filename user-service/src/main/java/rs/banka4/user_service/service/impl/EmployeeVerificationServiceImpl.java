package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.config.RabbitMqConfig;
import rs.banka4.user_service.dto.NotificationTransferDto;
import rs.banka4.user_service.dto.requests.EmployeeVerificationRequestDto;
import rs.banka4.user_service.exceptions.NotFound;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.VerificationCode;
import rs.banka4.user_service.service.abstraction.EmployeeService;
import rs.banka4.user_service.service.abstraction.EmployeeVerificationService;
import rs.banka4.user_service.utils.MessageHelper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeVerificationServiceImpl implements EmployeeVerificationService {

    private final VerificationCodeService verificationCodeService;
    private final EmployeeService employeeService;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public ResponseEntity<String> verifyEmployeeAccount(EmployeeVerificationRequestDto request) {
        Optional<VerificationCode> optionalVerificationCode = verificationCodeService.validateVerificationCode(request.verificationCode());
        if (optionalVerificationCode.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid or expired verification code.");
        }

        VerificationCode verificationCode = optionalVerificationCode.get();

        Optional<Employee> employeeOpt = employeeService.findEmployee(verificationCode.getEmail());
        if (employeeOpt.isEmpty()) {
            throw new NotFound("Employee not found.");
        }

        Employee employee = employeeOpt.get();

        employeeService.activateEmployeeAccount(employee, request.password());

        NotificationTransferDto message = MessageHelper.createAccountActivationMessage(
                employee.getEmail(), employee.getFirstName(), verificationCode.getCode());
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.ROUTING_KEY, message);

        verificationCodeService.markCodeAsUsed(verificationCode);

        return ResponseEntity.ok("Account activated successfully.");
    }

    @Override
    public ResponseEntity<String> forgotPassword(String email) {
        VerificationCode verificationCode = verificationCodeService.createVerificationCode(email);

        Optional<Employee> employeeOpt = employeeService.findEmployee(email);
        if (employeeOpt.isEmpty()) {
            throw new NotFound("Employee not found.");
        }

        Employee employee = employeeOpt.get();

        NotificationTransferDto message = MessageHelper.createForgotPasswordMessage(
                email, employee.getFirstName(), verificationCode.getCode());
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.ROUTING_KEY, message);

        return ResponseEntity.ok("Verification code sent to email.");
    }
}
