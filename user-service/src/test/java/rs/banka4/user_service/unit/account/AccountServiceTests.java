package rs.banka4.user_service.unit.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.AccountDto;
import rs.banka4.user_service.generator.AccountObjectMother;
import rs.banka4.user_service.mapper.BasicAccountMapper;
import rs.banka4.user_service.models.Account;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.service.impl.AccountServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccountServiceTests {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private BasicAccountMapper basicAccountMapper;
    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAccountSuccess() {
        // Arrange
        UUID accountId = UUID.fromString("4fe1f3ce-25e2-4b1c-b1f0-bec874e96555");
        Account account = new Account();
        account.setId(accountId);
        AccountDto accountDto = AccountObjectMother.generateBasicAccountDto();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(basicAccountMapper.toDto(account)).thenReturn(accountDto);

        // Act
        ResponseEntity<AccountDto> response = accountService.getAccount(accountId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(accountId, response.getBody().id());
        verify(accountRepository, times(1)).findById(accountId);
        verify(basicAccountMapper, times(1)).toDto(account);
    }

    @Test
    void testGetAccountNotFound() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<AccountDto> response = accountService.getAccount(accountId);

        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(accountRepository, times(1)).findById(accountId);
        verifyNoInteractions(basicAccountMapper);
    }
}