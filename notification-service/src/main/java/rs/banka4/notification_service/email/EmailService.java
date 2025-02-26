package rs.banka4.notification_service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;

    public void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        emailSender.send(message);
    }

    @RabbitListener(queues = "${rabbitmq.queue.email.name}")
    public void processEmailMessage(EmailDetailDto emailDetailDto) throws MessagingException {
        String to = emailDetailDto.recipient();
        String subject = setSubject(emailDetailDto.topic());
        String body = generateEmailBody(emailDetailDto);
        sendEmail(to, subject, body);
    }

    public String generateEmailBody(EmailDetailDto emailDetailDto) {
        String templateName = emailDetailDto.topic();
        String template = loadEmailTemplate(templateName);

        String body = template;
        if (emailDetailDto.params() != null) {
            for (Map.Entry<String, Object> entry : emailDetailDto.params().entrySet()) {
                body = body.replace("{{" + entry.getKey() + "}}", entry.getValue().toString());
            }
        }

        return body;
    }

    public String loadEmailTemplate(String templateName) {
        ClassPathResource resource = new ClassPathResource("templates/" + templateName + ".html");
        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error loading email template " + templateName, e);
        }
    }

    private String setSubject(String topic) {
        // In future maybe we have more than two html for sending email
        return switch (topic) {
            case "account-activation" -> Subjects.ACTIVATION.getSubject();
            case "password-reset" -> Subjects.PASSWORD_RESET.getSubject();
            default -> "";
        };
    }
}
