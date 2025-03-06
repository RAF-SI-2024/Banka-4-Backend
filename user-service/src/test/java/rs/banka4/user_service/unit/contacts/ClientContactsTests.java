package rs.banka4.user_service.unit.contacts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.ClientContactDto;
import rs.banka4.user_service.dto.requests.ClientContactRequest;
import rs.banka4.user_service.exceptions.NotFound;
import rs.banka4.user_service.generator.AccountObjectMother;
import rs.banka4.user_service.generator.ClientObjectMother;
import rs.banka4.user_service.mapper.ContactMapper;
import rs.banka4.user_service.models.Account;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.abstraction.AccountService;
import rs.banka4.user_service.service.impl.ClientServiceImpl;
import rs.banka4.user_service.utils.TestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientContactsTests {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ContactMapper contactMapper;
    @Mock
    private AccountService accountService;

    @InjectMocks
    private ClientServiceImpl clientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllContactsSuccess() {
        // Arrange
        Client client = ClientObjectMother.generateClientWithAllAttributes();
        Account account = AccountObjectMother.generateBasicAccount();
        ClientContactDto contactDto = ClientObjectMother.createClientContactDto();
        client.getSavedContacts().add(account);

        TestUtils.runWithMockedAuth(mockedAuth -> {
            when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
            when(contactMapper.toClientContactDto(account)).thenReturn(contactDto);

            // Act
            ResponseEntity<Page<ClientContactDto>> response = clientService.getAllContacts(PageRequest.of(0, 10));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            assertEquals(contactDto, response.getBody().getContent().getFirst());

            verify(clientRepository, times(1)).findById(client.getId());
            verify(contactMapper, times(1)).toClientContactDto(account);
        });
    }

    @Test
    void testGetAllClientNotFound() {
        // Arrange
        TestUtils.runWithMockedAuth(mockedAuth -> {
            String clientId = "6b105ac4-10fb-4bcf-abfb-96f4916be227";
            Pageable pageable = PageRequest.of(0, 10);
            when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NotFound.class, () -> clientService.getAllContacts(pageable));
        });
    }

    @Test
    void testGetAllNoContactsExist() {
        // Arrange
        TestUtils.runWithMockedAuth(mockedAuth -> {
            Pageable pageable = PageRequest.of(0, 10);
            Client client = ClientObjectMother.generateClientWithEmptyContacts();

            when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));

            // Act
            ResponseEntity<Page<ClientContactDto>> response = clientService.getAllContacts(pageable);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(0, response.getBody().getTotalElements());
        });
    }

    @Test
    void testCreateContactSuccessfully() {
        // Arrange
        Client client = ClientObjectMother.generateClientWithAllAttributes();
        Account account = AccountObjectMother.generateBasicAccount();
        ClientContactRequest request = new ClientContactRequest(account.getAccountNumber());

        TestUtils.runWithMockedAuth(mockedAuth -> {
            when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
            when(accountService.getAccountByAccountNumber(account.getAccountNumber())).thenReturn(account);

            // Act
            ResponseEntity<Void> response = clientService.createContact(request);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(client.getAccounts().contains(account));
        });
    }

    @Test
    void testCreateContactAccountNotFound() {
        // Arrange
        String accountNumber = "123456789";
        Client client = ClientObjectMother.generateClientWithAllAttributes();
        ClientContactRequest request = new ClientContactRequest(accountNumber);

        TestUtils.runWithMockedAuth(mockedAuth -> {
            when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
            when(accountService.getAccountByAccountNumber(accountNumber)).thenThrow(NotFound.class);

            // Act & Assert
            assertThrows(NotFound.class, () -> clientService.createContact(request));
        });
    }

    @Test
    void testDeleteContactSuccessfully() {
        // Arrange
        Client client = ClientObjectMother.generateClientWithAllAttributes();
        Account account = AccountObjectMother.generateBasicAccount();

        TestUtils.runWithMockedAuth(mockedAuth -> {
            when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
            when(accountService.getAccountByAccountNumber(account.getAccountNumber())).thenReturn(account);
            doNothing().when(clientRepository).deleteContactFromClient(client.getId(), String.valueOf(account.getId()));

            // Act
            ResponseEntity<Void> response = clientService.deleteContact(account.getAccountNumber());

            // Assert
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(clientRepository).deleteContactFromClient(client.getId(), String.valueOf(account.getId()));
        });
    }

    @Test
    void testDeleteContactClientNotFound() {
        // Arrange
        String accountNumber = "123456789";

        TestUtils.runWithMockedAuth(mockedAuth -> {
            when(clientRepository.findById(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NotFound.class, () -> clientService.deleteContact(accountNumber));
        });
    }

    @Test
    void testDeleteContactAccountNotFound() {
        // Arrange
        String accountNumber = "123456789";
        Client client = ClientObjectMother.generateClientWithAllAttributes();

        TestUtils.runWithMockedAuth(mockedAuth -> {
            when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
            when(accountService.getAccountByAccountNumber(accountNumber)).thenThrow(NotFound.class);

            // Act & Assert
            assertThrows(NotFound.class, () -> clientService.deleteContact(accountNumber));
        });
    }

}
