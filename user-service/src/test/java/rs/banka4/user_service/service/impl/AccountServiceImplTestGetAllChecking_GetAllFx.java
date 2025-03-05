package rs.banka4.user_service.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.mapper.BasicAccountMapper;
import rs.banka4.user_service.models.Account;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.models.Currency;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.utils.JwtUtil;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTestGetAllChecking_GetAllFx {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BasicAccountMapper accountMapper;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Page<Account> checkingAccounts;
    private Page<Account> fxAccounts;

    @BeforeEach
    void setUp() {
        Account account1 = mock(Account.class);
        Account account2 = mock(Account.class);
        checkingAccounts = new PageImpl<>(List.of(account1));
        fxAccounts = new PageImpl<>(List.of(account2));
    }

    @Test
    void testGetAllChecking_AsEmployee() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(authentication.getCredentials()).thenReturn("employee-token");
        when(jwtUtil.extractRole("employee-token")).thenReturn("employee");
        when(accountRepository.findAllByCurrency_Code(Currency.Code.RSD, pageRequest)).thenReturn(checkingAccounts);
        when(accountMapper.toDto(any(Account.class))).thenReturn(mock(AccountDto.class));

        ResponseEntity<Page<AccountDto>> response = accountService.getAllChecking(authentication, pageRequest);

        assertEquals(200, response.getStatusCodeValue());
        verify(accountRepository, times(1)).findAllByCurrency_Code(Currency.Code.RSD, pageRequest);
    }

    @Test
    void testGetAllFx_AsClient() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Client mockClient = mock(Client.class);
        when(authentication.getCredentials()).thenReturn("client-token");
        when(jwtUtil.extractRole("client-token")).thenReturn("client");
        when(jwtUtil.extractUsername("client-token")).thenReturn("client@example.com");
        when(clientRepository.findByEmail("client@example.com")).thenReturn(Optional.of(mockClient));
        when(accountRepository.findAllByClientAndCurrency_CodeNot(mockClient, Currency.Code.RSD, pageRequest)).thenReturn(fxAccounts);
        when(accountMapper.toDto(any(Account.class))).thenReturn(mock(AccountDto.class));

        ResponseEntity<Page<AccountDto>> response = accountService.getAllFx(authentication, pageRequest);

        assertEquals(200, response.getStatusCodeValue());
        verify(accountRepository, times(1)).findAllByClientAndCurrency_CodeNot(mockClient, Currency.Code.RSD, pageRequest);
    }
    @Test
    void testGetAllFx_AsEmployee() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(authentication.getCredentials()).thenReturn("employee-token");
        when(jwtUtil.extractRole("employee-token")).thenReturn("employee");
        when(accountRepository.findAllByCurrency_CodeNot(Currency.Code.RSD, pageRequest)).thenReturn(fxAccounts);
        when(accountMapper.toDto(any(Account.class))).thenReturn(mock(AccountDto.class));

        ResponseEntity<Page<AccountDto>> response = accountService.getAllFx(authentication, pageRequest);

        assertEquals(200, response.getStatusCodeValue());
        verify(accountRepository, times(1)).findAllByCurrency_CodeNot(Currency.Code.RSD, pageRequest);
    }

    @Test
    void testGetAllChecking_AsClient() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Client mockClient = mock(Client.class);
        when(authentication.getCredentials()).thenReturn("client-token");
        when(jwtUtil.extractRole("client-token")).thenReturn("client");
        when(jwtUtil.extractUsername("client-token")).thenReturn("client@example.com");
        when(clientRepository.findByEmail("client@example.com")).thenReturn(Optional.of(mockClient));
        when(accountRepository.findAllByClientAndCurrency_Code(mockClient, Currency.Code.RSD, pageRequest)).thenReturn(checkingAccounts);
        when(accountMapper.toDto(any(Account.class))).thenReturn(mock(AccountDto.class));

        ResponseEntity<Page<AccountDto>> response = accountService.getAllChecking(authentication, pageRequest);

        assertEquals(200, response.getStatusCodeValue());
        verify(accountRepository, times(1)).findAllByClientAndCurrency_Code(mockClient, Currency.Code.RSD, pageRequest);
    }
}
