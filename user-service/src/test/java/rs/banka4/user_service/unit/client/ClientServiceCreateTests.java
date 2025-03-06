package rs.banka4.user_service.unit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.config.RabbitMqConfig;
import rs.banka4.user_service.dto.NotificationTransferDto;
import rs.banka4.user_service.dto.requests.CreateClientDto;
import rs.banka4.user_service.exceptions.DuplicateEmail;
import rs.banka4.user_service.mapper.ClientMapper;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.models.Privilege;
import rs.banka4.user_service.models.VerificationCode;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.impl.ClientServiceImpl;
import rs.banka4.user_service.service.impl.VerificationCodeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ClientServiceCreateTests {
    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientServiceImpl clientService;
    @Mock
    private VerificationCodeService verificationCodeService;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateClientSuccessfully() {
        CreateClientDto createClientDto = new CreateClientDto(
                "212",
                "John",
                "Doe",
                LocalDate.now(),
                "Male",
                "john.doe@example.com",
                "12313",
                "dasasd",
                Set.of(Privilege.SEARCH)
        );

        Client clientEntity = new Client();
        clientEntity.setEmail("john.doe@example.com");
        clientEntity.setFirstName("John");
        clientEntity.setLastName("Doe");

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCode("28dc2fac-ff11-4bca-83b6-ba7e6727fd9b");

        when(clientRepository.existsByEmail(createClientDto.email())).thenReturn(false);
        when(clientMapper.toEntity(createClientDto)).thenReturn(clientEntity);
        when(verificationCodeService.createVerificationCode(createClientDto.email()))
                .thenReturn(verificationCode);

        ResponseEntity<Void> response = clientService.createClient(createClientDto);

        verify(clientMapper).toEntity(createClientDto);
        verify(clientRepository).save(clientEntity);
        verify(verificationCodeService).createVerificationCode(createClientDto.email());
        verify(rabbitTemplate, times(1))
                .convertAndSend(eq(RabbitMqConfig.EXCHANGE_NAME),
                        eq(RabbitMqConfig.ROUTING_KEY),
                        any(NotificationTransferDto.class));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        CreateClientDto createClientDto = new CreateClientDto(
                "123",
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "Male",
                "john.doe@example.com",
                "12313",
                "dasasd",
                Set.of(Privilege.SEARCH)
        );

        when(clientRepository.existsByEmail(createClientDto.email())).thenReturn(true);

        assertThrows(DuplicateEmail.class, () -> clientService.createClient(createClientDto));

        verify(clientMapper, never()).toEntity(any(CreateClientDto.class));
        verify(clientRepository, never()).save(any(Client.class));
    }
}
