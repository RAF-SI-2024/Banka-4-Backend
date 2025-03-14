package rs.banka4.user_service.unit.loan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.currency.dtos.CurrencyDto;
import rs.banka4.user_service.domain.currency.mapper.CurrencyMapper;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.domain.loan.db.LoanType;
import rs.banka4.user_service.domain.loan.dtos.LoanFilterDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;
import rs.banka4.user_service.domain.loan.mapper.LoanMapper;
import rs.banka4.user_service.repositories.LoanRepository;
import rs.banka4.user_service.service.impl.LoanServiceImpl;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
public class GetAllLoansTests {
    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanMapper loanMapper;
    @InjectMocks
    private LoanServiceImpl loanService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    List<LoanInformationDto> mockLoans = List.of(
            new LoanInformationDto(
                    1234567L, LoanType.CASH, BigDecimal.valueOf(10000), 60,
                    BigDecimal.valueOf(5.5), BigDecimal.valueOf(6.0),
                    LocalDate.now(), LocalDate.now().plusYears(5),
                    BigDecimal.valueOf(500), LocalDate.now().plusMonths(1),
                    BigDecimal.valueOf(5000),
                    new CurrencyDto(UUID.randomUUID(), "Euro", "EUR", "European currency", true, Currency.Code.EUR),
                    LoanStatus.APPROVED,
                    Loan.InterestType.FIXED
            ),
            new LoanInformationDto(
                    1234967L, LoanType.CASH, BigDecimal.valueOf(10000), 60,
                    BigDecimal.valueOf(5.5), BigDecimal.valueOf(6.0),
                    LocalDate.now(), LocalDate.now().plusYears(5),
                    BigDecimal.valueOf(500), LocalDate.now().plusMonths(1),
                    BigDecimal.valueOf(5000),
                    new CurrencyDto(UUID.randomUUID(), "Euro", "€", "Official currency of the eurozone", true, Currency.Code.EUR),
                    LoanStatus.APPROVED,
                    Loan.InterestType.FIXED

            ), new LoanInformationDto(
                    4434967L, LoanType.CASH, BigDecimal.valueOf(10000), 60,
                    BigDecimal.valueOf(5.5), BigDecimal.valueOf(6.0),
                    LocalDate.now(), LocalDate.now().plusYears(5),
                    BigDecimal.valueOf(500), LocalDate.now().plusMonths(1),
                    BigDecimal.valueOf(5000),
                    new CurrencyDto(UUID.randomUUID(), "Euro", "€", "Official currency of the eurozone", true, Currency.Code.EUR),
                    LoanStatus.APPROVED,
                    Loan.InterestType.FIXED
            )
    );
    @Test
    void whenStatusIsProcessing_thenSortByAgreementDateDescending() {
        LoanFilterDto filterDto = new LoanFilterDto(LoanType.CASH, LoanStatus.PROCESSING, "ACC123");
        PageRequest basePageRequest = PageRequest.of(0, 5); // base page request (without sort)

        Loan loan1 = new Loan();
        loan1.setLoanNumber(111L);
        loan1.setAgreementDate(LocalDate.of(2025, 3, 10));

        Account account1 = new Account();
        account1.setAccountNumber("12");
        loan1.setAccount(account1);

        Loan loan2 = new Loan();
        loan2.setLoanNumber(222L);
        loan2.setAgreementDate(LocalDate.of(2025, 1, 10));
        Account account2 = new Account();
        account2.setAccountNumber("23");
        loan2.setAccount(account2);

        List<Loan> loans = List.of(loan1, loan2);
        Page<Loan> loanPage = new PageImpl<>(loans, basePageRequest.withSort(Sort.by("agreementDate").descending()), loans.size());

        when(loanRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(loanPage);

        LoanInformationDto dto1 = mockLoans.get(0);
        LoanInformationDto dto2 = mockLoans.get(1);
        when(loanMapper.toDto(loan1, CurrencyMapper.INSTANCE)).thenReturn(dto1);
        when(loanMapper.toDto(loan2,CurrencyMapper.INSTANCE)).thenReturn(dto2);

        ResponseEntity<Page<LoanInformationDto>> response = loanService.getAllLoans(basePageRequest, filterDto);

        assertNotNull(response);
        Page<LoanInformationDto> resultPage = response.getBody();
        assertNotNull(resultPage);
        assertEquals(2, resultPage.getContent().size());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(loanRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();

        Sort.Order order = capturedPageable.getSort().getOrderFor("agreementDate");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void whenStatusIsNull_thenSortByAccountAccountNumber() {

        LoanFilterDto filterDto = new LoanFilterDto(LoanType.CASH, null, "ACC123");
        PageRequest basePageRequest = PageRequest.of(0, 5);

        Loan loan1 = new Loan();
        loan1.setLoanNumber(111L);
        Account account1 = new Account();
        account1.setAccountNumber("12");
        loan1.setAccount(account1);

        Loan loan2 = new Loan();
        loan2.setLoanNumber(222L);
        Account account2 = new Account();
        account2.setAccountNumber("12");
        loan2.setAccount(account2);

        List<Loan> loans = List.of(loan1, loan2);
        Page<Loan> loanPage = new PageImpl<>(loans, basePageRequest.withSort(Sort.by("account.accountNumber")), loans.size());
        when(loanRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(loanPage);

        LoanInformationDto dto1 = mockLoans.get(0);
        LoanInformationDto dto2 = mockLoans.get(1);
        when(loanMapper.toDto(loan1,CurrencyMapper.INSTANCE)).thenReturn(dto1);
        when(loanMapper.toDto(loan2,CurrencyMapper.INSTANCE)).thenReturn(dto2);

        ResponseEntity<Page<LoanInformationDto>> response = loanService.getAllLoans(basePageRequest, filterDto);

        assertNotNull(response);
        Page<LoanInformationDto> resultPage = response.getBody();
        assertNotNull(resultPage);
        assertEquals(2, resultPage.getContent().size());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(loanRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();

        Sort.Order order = capturedPageable.getSort().getOrderFor("account.accountNumber");
        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }
}
