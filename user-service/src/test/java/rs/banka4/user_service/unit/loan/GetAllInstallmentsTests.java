package rs.banka4.user_service.unit.loan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import rs.banka4.rafeisen.common.exceptions.jwt.Unauthorized;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanInstallment;
import rs.banka4.user_service.domain.loan.db.PaymentStatus;
import rs.banka4.user_service.domain.loan.dtos.LoanInstallmentDto;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.exceptions.loan.LoanNotFound;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.LoanInstallmentRepository;
import rs.banka4.user_service.repositories.LoanRepository;
import rs.banka4.user_service.service.abstraction.JwtService;
import rs.banka4.user_service.service.impl.LoanInstallmentServiceImpl;

public class GetAllInstallmentsTests {
    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private LoanInstallmentServiceImpl loanService;

    private Loan testLoan;
    private Client testClient;
    private Client testClient2;
    private final Long testLoanNumber = 123L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testLoan = new Loan();
        Account testAccount = new Account();
        testClient = new Client();
        testClient2 = new Client();

        testClient2.setEmail("email2");
        testClient2.setId(UUID.fromString("053f4cd3-0ece-4bcb-a667-b9ded5e2a211"));
        testClient.setEmail("email");
        testClient.setId(UUID.fromString("a0eabc83-07f8-4fad-92ab-4e879f865f36"));
        testAccount.setId(UUID.fromString("f3c265ee-f1d9-4f30-b0ca-340baae25155"));
        testAccount.setClient(testClient);
        testLoan.setId(UUID.fromString("6108c3f1-8624-4817-bfd8-dd28a892776e"));
        testLoan.setLoanNumber(testLoanNumber);
        testLoan.setAccount(testAccount);
    }

    @Test
    void testGetInstallmentsForLoan_UpcomingInstallmentExists() {

        when(loanRepository.findByLoanNumber(eq(testLoanNumber))).thenReturn(Optional.of(testLoan));
        when(jwtService.extractRole(anyString())).thenReturn("client");
        when(jwtService.extractUserId(anyString())).thenReturn(
            UUID.fromString("a0eabc83-07f8-4fad-92ab-4e879f865f36")
        );
        when(clientRepository.findById(UUID.fromString("a0eabc83-07f8-4fad-92ab-4e879f865f36")))
            .thenReturn(Optional.ofNullable(testClient));

        LoanInstallment upcoming = new LoanInstallment();
        upcoming.setId(UUID.randomUUID());
        upcoming.setExpectedDueDate(LocalDate.of(2025, 11, 18));
        upcoming.setPaymentStatus(PaymentStatus.UNPAID);

        when(loanInstallmentRepository.findAll(any(Specification.class), any(PageRequest.class)))
            .thenReturn(
                new PageImpl<>(
                    List.of(upcoming),
                    PageRequest.of(
                        0,
                        10,
                        Sort.by("expectedDueDate")
                            .ascending()
                    ),
                    1
                )
            );

        when(loanInstallmentRepository.findAll(any(Specification.class), any(PageRequest.class)))
            .thenReturn(
                new PageImpl<>(
                    List.of(upcoming),
                    PageRequest.of(
                        0,
                        10,
                        Sort.by("expectedDueDate")
                            .ascending()
                    ),
                    1
                )
            );

        Page<LoanInstallmentDto> result =
            loanService.getInstallmentsForLoan(testLoanNumber, 0, 10, anyString());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(loanRepository).findByLoanNumber(eq(testLoanNumber));
        verify(loanInstallmentRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void testGetInstallmentsForLoan_NoUpcomingInstallment() {
        when(loanRepository.findByLoanNumber(eq(testLoanNumber))).thenReturn(Optional.of(testLoan));
        when(jwtService.extractRole(anyString())).thenReturn("client");
        when(clientRepository.findById(UUID.fromString("a0eabc83-07f8-4fad-92ab-4e879f865f36")))
            .thenReturn(Optional.ofNullable(testClient));
        when(jwtService.extractUserId(anyString())).thenReturn(
            UUID.fromString("a0eabc83-07f8-4fad-92ab-4e879f865f36")
        );
        Page<LoanInstallment> emptyPage =
            new PageImpl<>(
                List.of(),
                PageRequest.of(
                    0,
                    10,
                    Sort.by("expectedDueDate")
                        .ascending()
                ),
                0
            );
        when(loanInstallmentRepository.findAll(any(Specification.class), any(PageRequest.class)))
            .thenReturn(emptyPage);

        Page<LoanInstallmentDto> result =
            loanService.getInstallmentsForLoan(testLoanNumber, 0, 10, anyString());

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(loanRepository).findByLoanNumber(eq(testLoanNumber));
        verify(loanInstallmentRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void testGetInstallmentsForLoan_LoanNotFound() {
        when(loanRepository.findByLoanNumber(eq(testLoanNumber))).thenReturn(Optional.empty());
        when(jwtService.extractRole(anyString())).thenReturn("client");
        when(jwtService.extractUserId(anyString())).thenReturn(
            UUID.fromString("a0eabc83-07f8-4fad-92ab-4e879f865f36")
        );
        when(clientRepository.findById(UUID.fromString("a0eabc83-07f8-4fad-92ab-4e879f865f36")))
            .thenReturn(Optional.ofNullable(testClient));

        assertThrows(
            LoanNotFound.class,
            () -> loanService.getInstallmentsForLoan(testLoanNumber, 0, 10, anyString())
        );

        verify(loanRepository).findByLoanNumber(eq(testLoanNumber));
    }

    @Test
    void testGetInstallmentsForLoan_Unauthorized() {
        when(loanRepository.findByLoanNumber(eq(testLoanNumber))).thenReturn(
            Optional.ofNullable(testLoan)
        );
        when(jwtService.extractRole(anyString())).thenReturn("client");
        when(jwtService.extractUserId(anyString())).thenReturn(
            UUID.fromString("a0eabc83-07f8-4fad-92ab-4e879f865f36")
        );
        when(clientRepository.findById(UUID.fromString("a0eabc83-07f8-4fad-92ab-4e879f865f36")))
            .thenReturn(Optional.ofNullable(testClient2));

        assertThrows(
            Unauthorized.class,
            () -> loanService.getInstallmentsForLoan(testLoanNumber, 0, 10, anyString())
        );

        verify(loanRepository).findByLoanNumber(eq(testLoanNumber));
    }
}
