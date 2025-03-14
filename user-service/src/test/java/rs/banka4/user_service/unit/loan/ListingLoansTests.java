package rs.banka4.user_service.unit.loan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.account.dtos.AccountDto;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;
import rs.banka4.user_service.domain.loan.mapper.LoanMapper;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.exceptions.NullPageRequest;
import rs.banka4.user_service.exceptions.loan.NoLoansOnAccount;
import rs.banka4.user_service.exceptions.user.client.ClientNotFound;
import rs.banka4.user_service.generator.AccountObjectMother;
import rs.banka4.user_service.generator.LoanObjectMother;
import rs.banka4.user_service.repositories.LoanRepository;
import rs.banka4.user_service.service.abstraction.AccountService;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.service.impl.LoanServiceImpl;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

@ExtendWith(MockitoExtension.class)
public class ListingLoansTests {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ClientService clientService;

    @Mock
    private AccountService accountService;

    @Mock
    private LoanMapper loanMapper;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanServiceImpl loanService;

    private LoanApplicationDto loanApplicationDto;
    private Client client;
    private Account account;
    private AccountDto accountDto;


    @BeforeEach
    void setUp() {
        loanApplicationDto = LoanObjectMother.generateLoanApplicationDto();
        client = new Client();
        UUID clientId = UUID.randomUUID();
        client.setId(clientId);
        client.setEmail("test@example.com");
        client.setFirstName("OldFirstName");
        client.setLastName("OldLastName");

        UUID accountId = UUID.randomUUID();

        account = new Account();
        account.setId(accountId);
        account.setClient(client);
        account.setAccountNumber("444394438340549");
        account.setActive(true);

        accountDto = AccountObjectMother.generateBasicAccountDto();
    }

    @Test
    void getMyLoans_Success() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "amount"));
        when(jwtUtil.extractUsername(anyString())).thenReturn("test@example.com");
        when(clientService.getClientByEmail("test@example.com")).thenReturn(Optional.of(client));
        when(accountService.getAccountsForClient(anyString()))
                .thenReturn(Set.of(accountDto));

        Loan loan = new Loan();
        loan.setAccount(account);

        List<Loan> loans = List.of(loan);

        Page<Loan> loanPage = new PageImpl<>(loans, pageRequest, loans.size());

        when(loanRepository.findAll(any(Specification.class),any(PageRequest.class)))
                .thenReturn(loanPage);

        ResponseEntity<Page<LoanInformationDto>> response = loanService.getMyLoans(anyString(), pageRequest);

        assertNotNull(response);

        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getMyLoans_ClientNotFound() {
        when(jwtUtil.extractUsername(anyString())).thenReturn("test@example.com");
        when(clientService.getClientByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(ClientNotFound.class, () -> loanService.getMyLoans(anyString(), PageRequest.of(0, 10)));
    }

    @Test
    void invalidPageRequest(){
        assertThrows(NullPageRequest.class, () -> loanService.getMyLoans("Bearer token",null));
    }

    @Test
    void getMyLoans_NoLoansOnAccount() {
        when(jwtUtil.extractUsername(anyString())).thenReturn("test@example.com");
        when(clientService.getClientByEmail("test@example.com")).thenReturn(Optional.of(client));
        when(accountService.getAccountsForClient(anyString())).thenReturn(Set.of());

        assertThrows(NoLoansOnAccount.class, () -> loanService.getMyLoans(anyString(), PageRequest.of(0, 10)));
    }

}
