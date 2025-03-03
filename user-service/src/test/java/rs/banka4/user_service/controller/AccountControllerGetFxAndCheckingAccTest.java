package rs.banka4.user_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.AccountDto;
import rs.banka4.user_service.dto.CurrencyDto;
import rs.banka4.user_service.models.Currency;
import rs.banka4.user_service.service.abstraction.AccountService;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerGetFxAndCheckingAccTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private Page<AccountDto> checkingAccounts;
    private Page<AccountDto> fxAccounts;
    private CurrencyDto rsdCurrency;
    private CurrencyDto eurCurrency;

    @BeforeEach
    void setUp() {
        rsdCurrency = new CurrencyDto("1", "Serbian Dinar", "RSD", "RSD currency", true, Currency.Code.RSD, Set.of("Serbia"));
        eurCurrency = new CurrencyDto("2", "Euro", "EUR", "Euro currency", true, Currency.Code.EUR, Set.of("EU"));

        AccountDto checkingAccount = new AccountDto("id1", "1234567890", null, null, null, null, null, true, null, null, null, rsdCurrency, null, null, null);
        AccountDto fxAccount = new AccountDto("id2", "0987654321", null, null, null, null, null, true, null, null, null, eurCurrency, null, null, null);
        AccountDto fxAccount2 = new AccountDto("id4", "5566778899", null, null, null, null, null, true, null, null, null, eurCurrency, null, null, null);
        AccountDto checkingAccount2 = new AccountDto("id3", "1122334455", null, null, null, null, null, true, null, null, null, rsdCurrency, null, null, null);

        checkingAccounts = new PageImpl<>(List.of(checkingAccount, checkingAccount2));
        fxAccounts = new PageImpl<>(List.of(fxAccount, fxAccount2));
    }

    @Test
    void getAllChecking_ShouldReturnOnlyCheckingAccounts() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(accountService.getAllChecking(pageRequest)).thenReturn(ResponseEntity.ok(checkingAccounts));

        ResponseEntity<Page<AccountDto>> response = accountController.getAllChecking(0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        assertEquals(2, response.getBody().getContent().size());
        response.getBody().getContent().forEach(account ->
                assertEquals(Currency.Code.RSD, account.currency().code())
        );
        verify(accountService, times(1)).getAllChecking(pageRequest);
    }

    @Test
    void getAllFx_ShouldReturnOnlyFxAccounts() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(accountService.getAllFx(pageRequest)).thenReturn(ResponseEntity.ok(fxAccounts));

        ResponseEntity<Page<AccountDto>> response = accountController.getAllFx(0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        assertEquals(2, response.getBody().getContent().size());
        response.getBody().getContent().forEach(account ->
                assertNotEquals(Currency.Code.RSD, account.currency().code())
        );
        verify(accountService, times(1)).getAllFx(pageRequest);
    }

    @Test
    void getAllChecking_ShouldReturnForbidden() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(accountService.getAllChecking(pageRequest)).thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).build());

        ResponseEntity<Page<AccountDto>> response = accountController.getAllChecking(0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAllFx_ShouldReturnUnauthorized() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(accountService.getAllFx(pageRequest)).thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());

        ResponseEntity<Page<AccountDto>> response = accountController.getAllFx(0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
    @Test
    void getAllChecking_ShouldRespectPagination() {
        PageRequest pageRequest = PageRequest.of(1, 1);
        when(accountService.getAllChecking(pageRequest)).thenReturn(ResponseEntity.ok(new PageImpl<>(List.of(checkingAccounts.getContent().get(1)), pageRequest, checkingAccounts.getTotalElements())));

        ResponseEntity<Page<AccountDto>> response = accountController.getAllChecking(1, 1);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("id3", response.getBody().getContent().get(0).id());
        verify(accountService, times(1)).getAllChecking(pageRequest);
    }

    @Test
    void getAllFx_ShouldRespectPagination() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        when(accountService.getAllFx(pageRequest)).thenReturn(ResponseEntity.ok(new PageImpl<>(List.of(fxAccounts.getContent().get(0)), pageRequest, fxAccounts.getTotalElements())));

        ResponseEntity<Page<AccountDto>> response = accountController.getAllFx(0, 1);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("id2", response.getBody().getContent().get(0).id());
        verify(accountService, times(1)).getAllFx(pageRequest);
    }

}
