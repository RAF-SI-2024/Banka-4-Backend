package rs.banka4.user_service.unit.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
import rs.banka4.user_service.domain.user.client.mapper.ClientMapper;
import rs.banka4.user_service.exceptions.user.NotAuthenticated;
import rs.banka4.user_service.exceptions.user.NotFound;
import rs.banka4.user_service.exceptions.user.client.NonexistantSortByField;
import rs.banka4.user_service.generator.ClientObjectMother;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.UserTotpSecretRepository;
import rs.banka4.user_service.service.impl.ClientServiceImpl;
import rs.banka4.user_service.utils.JwtUtil;

public class ClientServiceGetTests {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private ClientMapper clientMapper;
    @InjectMocks
    private ClientServiceImpl clientService;
    @Mock
    private UserTotpSecretRepository userTotpSecretRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static Stream<Arguments> provideFilters() {
        return Stream.of(
            Arguments.of("John", "Doe", "john.doe@example.com", "123-456-7890", "default"),
            Arguments.of("John", "", "", "", "firstName"),
            Arguments.of("", "Doe", "", "", "lastName"),
            Arguments.of("", "", "john.doe@example.com", "", "email"),
            Arguments.of("", "", "", "123-456-7890", "default"),
            Arguments.of(null, null, null, null, "default")
        );
    }

    @ParameterizedTest
    @MethodSource("provideFilters")
    void testGetClientsWithFilters(
        String firstName,
        String lastName,
        String email,
        String phone,
        String sortBy
    ) {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10);
        UUID clientId = UUID.randomUUID();
        Client client = ClientObjectMother.generateClient(clientId, "john.doe@example.com");
        Page<Client> clientPage = new PageImpl<>(List.of(client), pageRequest, 1);

        when(clientRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(
            clientPage
        );

        // Act
        ResponseEntity<Page<ClientDto>> response =
            clientService.getClients(firstName, lastName, email, phone, sortBy, pageRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(
            1,
            response.getBody()
                .getTotalElements()
        );
    }

    @ParameterizedTest
    @MethodSource("provideFilters")
    void testGetClientsWithInvalidSortField(
        String firstName,
        String lastName,
        String email,
        String phone,
        String sortBy
    ) {
        // Arrange
        String invalidSort = "invalidField";
        PageRequest pageRequest = PageRequest.of(0, 10);

        // Act & Assert
        assertThrows(
            NonexistantSortByField.class,
            () -> clientService.getClients(
                firstName,
                lastName,
                email,
                phone,
                invalidSort,
                pageRequest
            )
        );
    }

    @Test
    void testGetClientByIdSuccess() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        Client client = ClientObjectMother.generateClient(clientId, "email@test.com");
        ClientDto clientDto = ClientObjectMother.generateBasicClientDto(clientId, "email@test.com");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(clientDto);
        clientRepository.save(client);

        // Act
        ClientDto result = clientService.getClientById(clientId);

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.id());
    }

    @Test
    void testGetClientByIdNotFound() {
        // Arrange
        UUID clientId = UUID.randomUUID();

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFound.class, () -> clientService.getClientById(clientId));
    }

    @Test
    void testGetClientByEmailSuccess() {
        // Arrange
        String email = "email@test.com";
        Client client = ClientObjectMother.generateClient(UUID.randomUUID(), email);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        // Act
        Optional<Client> result = clientService.getClientByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(
            email,
            result.get()
                .getEmail()
        );
    }

    @Test
    void testGetMeSuccess() {
        // Arrange
        String tokenStr = "validToken";
        String bearerToken = "Bearer " + tokenStr;
        String email = "email@test.com";
        Client client = ClientObjectMother.generateClient(UUID.randomUUID(), email);
        ClientDto clientDto = ClientObjectMother.generateBasicClientDto(client.getId(), email);

        when(jwtUtil.extractUsername(tokenStr)).thenReturn(email);
        when(jwtUtil.isTokenExpired(tokenStr)).thenReturn(false);
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        // Act
        ClientDto result = clientService.getMe(bearerToken);

        // Assert
        assertNotNull(result);
        assertEquals(client.getId(), result.id());
    }

    @Test
    void testGetMeTokenExpired() {
        // Arrange
        String tokenStr = "expiredToken";
        String bearerToken = "Bearer " + tokenStr;
        String email = "email@test.com";

        when(jwtUtil.extractUsername(tokenStr)).thenReturn(email);
        when(jwtUtil.isTokenExpired(tokenStr)).thenReturn(true);

        // Act & Assert
        assertThrows(NotAuthenticated.class, () -> clientService.getMe(bearerToken));
    }

    @Test
    void testGetMeNotFound() {
        // Arrange
        String tokenStr = "validToken";
        String bearerToken = "Bearer " + tokenStr;
        String email = "email@test.com";

        when(jwtUtil.extractUsername(tokenStr)).thenReturn(email);
        when(jwtUtil.isTokenExpired(tokenStr)).thenReturn(false);
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFound.class, () -> {
            try (MockedStatic<ClientMapper> ignored = mockStatic(ClientMapper.class)) {
                clientService.getMe(bearerToken);
            }
        });
    }
}
