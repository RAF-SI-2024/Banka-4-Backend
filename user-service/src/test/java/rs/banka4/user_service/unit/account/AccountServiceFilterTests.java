package rs.banka4.user_service.unit.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.account.dtos.AccountDto;
import rs.banka4.user_service.domain.account.mapper.AccountMapper;
import rs.banka4.user_service.generator.AccountObjectMother;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.service.impl.AccountServiceImpl;
import rs.banka4.user_service.utils.JwtUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class AccountServiceFilterTests {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static Stream<Arguments> provideFilters() {
        return Stream.of(
                Arguments.of("John", "Doe", "444394438340549", 1),
                Arguments.of("Jan", "Doe", null, 0),
                Arguments.of(null, "Smith", null, 0),
                Arguments.of("Jo", null, "444394438340549", 1),
                Arguments.of(null, null, null, 3)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFilters")
    void testGetAll(String firstName, String lastName, String accountNumber, int expectedSize) {
        // Arrange
        String token = "mocked-token";
        String role = "client";
        Account account1 = AccountObjectMother.generateBasicFromAccount();
        Account account2 = AccountObjectMother.generateBasicToAccount();
        Account account3 = AccountObjectMother.generateBasicFromAccount();
        account3.setAccountNumber("9876543210");

        PageRequest pageRequest = PageRequest.of(0, 10);

        when(jwtUtil.extractUsername(token)).thenReturn("john.doe@example.com");
        when(jwtUtil.extractRole(token)).thenReturn(role);

        ArgumentCaptor<Specification<Account>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        when(accountRepository.findAll(specCaptor.capture(), eq(pageRequest))).thenAnswer(invocation -> {
            List<Account> filteredAccounts = Stream.of(account1, account2, account3)
                    .filter(account -> {
                        if (firstName != null && !account.getClient().getFirstName().toLowerCase().contains(firstName.toLowerCase())) return false;
                        if (lastName != null && !account.getClient().getLastName().toLowerCase().contains(lastName.toLowerCase())) return false;
                        if (accountNumber != null && !account.getAccountNumber().toLowerCase().contains(accountNumber.toLowerCase())) return false;
                        return true;
                    })
                    .collect(Collectors.toList());
            return new PageImpl<>(filteredAccounts, pageRequest, filteredAccounts.size());
        });

        when(accountMapper.toDto(account1)).thenReturn(AccountObjectMother.generateBasicAccountDto());
        when(accountMapper.toDto(account2)).thenReturn(AccountObjectMother.generateBasicAccountDto());
        when(accountMapper.toDto(account3)).thenReturn(AccountObjectMother.generateBasicAccountDto());

        Authentication authentication = mock(Authentication.class);
        when(authentication.getCredentials()).thenReturn(token);

        // Act
        ResponseEntity<Page<AccountDto>> responseEntity = accountService.getAll(authentication, firstName, lastName, accountNumber, pageRequest);
        Page<AccountDto> result = responseEntity.getBody();

        // Assert
        assertNotNull(result);
        assertEquals(expectedSize, result.getTotalElements());
    }
}