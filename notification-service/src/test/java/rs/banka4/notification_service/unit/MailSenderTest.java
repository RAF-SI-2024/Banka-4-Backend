package rs.banka4.notification_service.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import rs.banka4.notification_service.email.EmailService;

public class MailSenderTest {

    @Mock
    private JavaMailSender emailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendHtmlEmail() throws MessagingException, IOException {
        String to = "test@example.com";
        String subject = "Test Subject";
        String htmlBody = "<h1>Test HTML Body</h1>";
        String textBody = "Test Text Body";

        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(emailSender)
            .send(mimeMessage);
        when(mimeMessage.getRecipients(MimeMessage.RecipientType.TO)).thenReturn(
            InternetAddress.parse(to)
        );
        when(mimeMessage.getSubject()).thenReturn(subject);
        when(mimeMessage.getContent()).thenReturn(htmlBody);

        emailService.sendEmail(to, subject, htmlBody, textBody);

        ArgumentCaptor<MimeMessage> mimeMessageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(emailSender, times(1)).send(mimeMessageCaptor.capture());

        MimeMessage sentMessage = mimeMessageCaptor.getValue();

        assertEquals(
            to,
            ((InternetAddress) sentMessage.getRecipients(MimeMessage.RecipientType.TO)[0])
                .getAddress()
        );
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(
            htmlBody,
            sentMessage.getContent()
                .toString()
        );
    }

    @Test
    void testSendPlainTextEmail() throws MessagingException, IOException {
        String to = "test@example.com";
        String subject = "Test Subject";
        String htmlBody = "<h1>Test HTML Body</h1>";
        String textBody = "Test Text Body";

        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(emailSender)
            .send(mimeMessage);
        when(mimeMessage.getRecipients(MimeMessage.RecipientType.TO)).thenReturn(
            InternetAddress.parse(to)
        );
        when(mimeMessage.getSubject()).thenReturn(subject);
        when(mimeMessage.getContent()).thenReturn(textBody);

        emailService.sendEmail(to, subject, htmlBody, textBody);

        ArgumentCaptor<MimeMessage> mimeMessageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(emailSender, times(1)).send(mimeMessageCaptor.capture());

        MimeMessage sentMessage = mimeMessageCaptor.getValue();

        assertEquals(
            to,
            ((InternetAddress) sentMessage.getRecipients(MimeMessage.RecipientType.TO)[0])
                .getAddress()
        );
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(
            textBody,
            sentMessage.getContent()
                .toString()
        );
    }

}
