package rs.banka4.user_service.service.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.config.RabbitMqConfig;
import rs.banka4.user_service.domain.auth.db.VerificationCode;
import rs.banka4.user_service.domain.auth.dtos.NotificationTransferDto;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.utils.MessageHelper;

@Service
@RequiredArgsConstructor
public class UserService {

    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final VerificationCodeService verificationCodeService;
    private final RabbitTemplate rabbitTemplate;

    public boolean existsByEmail(String email) {
        return employeeRepository.existsByEmail(email) || clientRepository.existsByEmail(email);
    }

    public boolean isPhoneNumberValid(String phoneNumber) {

        String regex = "^(\\+3816|06)(\\d{7,8}|(77|78)\\d{5,6})$";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(phoneNumber);

        return matcher.matches();

    }

    public void sendVerificationEmail(String firstName, String email) {
        VerificationCode verificationCode = verificationCodeService.createVerificationCode(email);

        if (verificationCode == null || verificationCode.getCode() == null) {
            throw new IllegalStateException(
                "Failed to generate verification code for email: " + email
            );
        }

        NotificationTransferDto message =
            MessageHelper.createAccountActivationMessage(
                email,
                firstName,
                verificationCode.getCode()
            );

        rabbitTemplate.convertAndSend(
            RabbitMqConfig.EXCHANGE_NAME,
            RabbitMqConfig.ROUTING_KEY,
            message
        );
    }
}
