package rs.banka4.user_service.unit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import rs.banka4.user_service.domain.auth.dtos.NotificationTransferDto;
import rs.banka4.user_service.domain.user.client.dtos.CreateClientDto;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.auth.db.VerificationCode;
import rs.banka4.user_service.exceptions.user.DuplicateEmail;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.impl.ClientServiceImpl;
import rs.banka4.user_service.service.impl.UserService;
import rs.banka4.user_service.service.impl.VerificationCodeService;
import rs.banka4.user_service.generator.ClientObjectMother;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ClientServiceCreateTests {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private UserService userService;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private VerificationCodeService verificationCodeService;
    @InjectMocks
    private ClientServiceImpl clientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);}

    @Test
    void testCreateClientSuccess() {
        // Arrange
        userService = new UserService(employeeRepository, clientRepository, verificationCodeService, rabbitTemplate);
        clientService = new ClientServiceImpl(userService, clientRepository, null, null, null, null, null);

        CreateClientDto createClientDto = ClientObjectMother.generateBasicCreateClientDto();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCode("123456");

        when(userService.existsByEmail(createClientDto.email())).thenReturn(false);
        when(clientRepository.findByEmail(createClientDto.email())).thenReturn(Optional.empty());
        when(verificationCodeService.createVerificationCode(createClientDto.email())).thenReturn(verificationCode);

        // Act
        clientService.createClient(createClientDto);

        // Assert
        verify(clientRepository, times(1)).save(any(Client.class));
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(NotificationTransferDto.class));
    }

    @Test
    void testCreateClientEmailAlreadyExists() {
        // Arrange
        CreateClientDto createClientDto = ClientObjectMother.generateBasicCreateClientDto();
        when(userService.existsByEmail(createClientDto.email())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateEmail.class, () -> clientService.createClient(createClientDto));

        // Verify
        verify(userService, times(1)).existsByEmail(createClientDto.email());
    }
}