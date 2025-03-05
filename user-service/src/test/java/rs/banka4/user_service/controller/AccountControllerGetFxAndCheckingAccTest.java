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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.models.Currency;
import rs.banka4.user_service.service.abstraction.AccountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerGetFxAndCheckingAccTest {

    @Mock
    private AccountService accountService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AccountController accountController;

    private Page<AccountDto> checkingAccounts;
    private Page<AccountDto> fxAccounts;

    @BeforeEach
    void setUp() {
        CurrencyDto currencyDto = new CurrencyDto("11111111-2222-3333-4444-555555555555", "Serbian Dinar", "RSD", "Currency used in Serbia", true, Currency.Code.RSD, Set.of("Serbia", "Montenegro"));
        AccountDto account1 = new AccountDto("11111111-2222-3333-4444-555555555555", "1234567890", new BigDecimal("1000.00"), new BigDecimal("800.00"), new BigDecimal("100.00"), LocalDate.of(2023, 1, 1), LocalDate.of(2028, 1, 1), true, AccountTypeDto.CheckingBusiness, new BigDecimal("100.00"), new BigDecimal("1000.00"), currencyDto, null, null, null);
        AccountDto account2 = new AccountDto("22222222-3333-4444-5555-666666666666", "0987654321", new BigDecimal("5000.00"), new BigDecimal("4500.00"), BigDecimal.ZERO, LocalDate.of(2022, 6, 15), LocalDate.of(2027, 6, 15), true, AccountTypeDto.FxBusiness, new BigDecimal("200.00"), new BigDecimal("5000.00"), currencyDto, null, null, null);
        checkingAccounts = new PageImpl<>(List.of(account1));
        fxAccounts = new PageImpl<>(List.of(account2));
    }

    @Test
    void testGetAllChecking_Success() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(accountService.getAllChecking(authentication, pageRequest)).thenReturn(ResponseEntity.ok(checkingAccounts));

        ResponseEntity<Page<AccountDto>> response = accountController.getAllChecking(0, 10, authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getTotalElements());
        verify(accountService, times(1)).getAllChecking(authentication, pageRequest);
    }

    @Test
    void testGetAllFx_Success() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(accountService.getAllFx(authentication, pageRequest)).thenReturn(ResponseEntity.ok(fxAccounts));

        ResponseEntity<Page<AccountDto>> response = accountController.getAllFx(0, 10, authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getTotalElements());
        verify(accountService, times(1)).getAllFx(authentication, pageRequest);
    }

}
