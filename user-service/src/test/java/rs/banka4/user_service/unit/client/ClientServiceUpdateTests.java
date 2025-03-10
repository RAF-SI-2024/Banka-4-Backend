package rs.banka4.user_service.unit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.banka4.user_service.domain.user.Privilege;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.dtos.UpdateClientDto;
import rs.banka4.user_service.exceptions.user.client.ClientNotFound;
import rs.banka4.user_service.exceptions.user.DuplicateEmail;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.impl.ClientServiceImpl;
import rs.banka4.user_service.service.impl.UserService;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceUpdateTests {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private ClientServiceImpl clientService;

    private Client existingClient;
    private UUID clientId;
    private UpdateClientDto updateClientDto;

    @BeforeEach
    public void setUp() {
        clientId = UUID.randomUUID();
        existingClient = new Client();
        existingClient.setId(clientId);
        existingClient.setEmail("old.email@example.com");
        existingClient.setFirstName("OldFirstName");
        existingClient.setLastName("OldLastName");

        updateClientDto = new UpdateClientDto(
                "NewFirstName",
                "NewLastName",
                LocalDate.of(1990, 1, 1),
                "Male",
                "new.email@example.com",
                "1234567890",
                "New Address",
                Set.of(Privilege.SEARCH)
        );
    }

    @Test
    public void testUpdateClientSuccess() {
        // Arrange
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(userService.existsByEmail(updateClientDto.email())).thenReturn(false);

        // Act
        clientService.updateClient(clientId, updateClientDto);

        // Assert
        verify(clientRepository).findById(clientId);
        verify(userService).existsByEmail(updateClientDto.email());
        verify(clientRepository).save(existingClient);

        assertEquals(updateClientDto.email(), existingClient.getEmail());
    }

    @Test
    public void testUpdateClientDuplicateEmail() {
        // Arrange
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(userService.existsByEmail(updateClientDto.email())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateEmail.class, () ->
                clientService.updateClient(clientId, updateClientDto)
        );

        verify(clientRepository).findById(clientId);
        verify(userService).existsByEmail(updateClientDto.email());
        verify(clientRepository, never()).save(any());
    }

    @Test
    public void testUpdateClientNotFound() {
        // Arrange
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClientNotFound.class, () ->
                clientService.updateClient(clientId, updateClientDto)
        );

        verify(clientRepository).findById(clientId);
        verify(userService, never()).existsByEmail(anyString());
        verify(clientRepository, never()).save(any());
    }
}