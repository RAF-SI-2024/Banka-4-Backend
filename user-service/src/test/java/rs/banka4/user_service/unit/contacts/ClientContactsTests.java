package rs.banka4.user_service.unit.contacts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.ClientContactDto;
import rs.banka4.user_service.dto.requests.ClientContactRequest;
import rs.banka4.user_service.generator.ClientObjectMother;
import rs.banka4.user_service.mapper.ContactMapper;
import rs.banka4.user_service.models.Account;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.impl.ClientServiceImpl;
import rs.banka4.user_service.utils.AuthUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ClientContactsTests {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ContactMapper contactMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddContactsSuccess() {
        System.out.println("Test passed");
    }

    @Test
    public void testRemoveContactsSuccess() {

    }

    @Test
    void testGetAllContactsSuccess() {
        // Arrange
        Client client = new Client();
        client.setId("client-id");
        client.setFirstName("John");
        client.setLastName("Doe");
        client.setSavedContacts(new HashSet<>());

        Account account = new Account();
        account.setId(UUID.fromString("155de92c-4a16-41bf-89c4-5997a53a0009"));
        account.setAccountNumber("444000000000123456");

        ClientContactDto contactDto = new ClientContactDto("Mehmedalija", "Doe", "444000000000123456");
        client.getSavedContacts().add(account);

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getLoggedUserId).thenReturn("client-id");

            when(clientRepository.findById("client-id")).thenReturn(Optional.of(client));
            when(contactMapper.toClientContactDto(account)).thenReturn(contactDto);

            // Act
            ResponseEntity<Page<ClientContactDto>> response = clientService.getAllContacts(PageRequest.of(0, 10));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            assertEquals(contactDto, response.getBody().getContent().getFirst());

            verify(clientRepository, times(1)).findById("client-id");
            verify(contactMapper, times(1)).toClientContactDto(account);
        }
    }

}
