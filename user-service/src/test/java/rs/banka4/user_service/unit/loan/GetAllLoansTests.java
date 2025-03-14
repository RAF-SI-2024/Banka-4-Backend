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
import rs.banka4.user_service.domain.loan.db.LoanRequest;
import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.domain.loan.db.LoanType;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationResponseDto;
import rs.banka4.user_service.domain.loan.dtos.LoanFilterDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;
import rs.banka4.user_service.domain.loan.mapper.LoanMapper;
import rs.banka4.user_service.repositories.LoanRepository;
import rs.banka4.user_service.repositories.LoanRequestRepository;
import rs.banka4.user_service.service.impl.LoanServiceImpl;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    @Mock
    private LoanRequestRepository loanRequestRepository;
    @InjectMocks
    private LoanServiceImpl loanService;
    private LoanRequest sampleLoanRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setAccountNumber("35123456789012345678");

        Loan loan = new Loan();
        loan.setLoanNumber(1234567L);

        sampleLoanRequest = new LoanRequest();
        sampleLoanRequest.setId(UUID.randomUUID());
        sampleLoanRequest.setAmount(new BigDecimal("10000"));
        sampleLoanRequest.setCurrency(new Currency());
        sampleLoanRequest.getCurrency().setCode(Currency.Code.EUR);
        sampleLoanRequest.getCurrency().setName("Euro");
        sampleLoanRequest.setPurposeOfLoan("Education");
        sampleLoanRequest.setMonthlyIncome(new BigDecimal("2500"));
        sampleLoanRequest.setEmploymentStatus("Permanent");
        sampleLoanRequest.setEmploymentPeriod(5);
        sampleLoanRequest.setRepaymentPeriod(60);
        sampleLoanRequest.setContactPhone("+381641234567");
        sampleLoanRequest.setAccount(account);
        sampleLoanRequest.setLoan(loan);
        sampleLoanRequest.setType(LoanType.CASH);
        sampleLoanRequest.setInterestType(Loan.InterestType.FIXED);
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
    void getAllLoansProcessing() {
        // Arrange: create a Page containing our sample LoanRequest
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<LoanRequest> loanRequestPage = new PageImpl<>(List.of(sampleLoanRequest), pageRequest, 1);

        // Stub repository call:
        when(loanRequestRepository.findAllWithLoans(any(), any(PageRequest.class)))
                .thenReturn(loanRequestPage);

        // Create a simple filter dto (values don't affect mapping here)
        LoanFilterDto filterDto = new LoanFilterDto(null, null, null);

        // Act: call the service method
        ResponseEntity<Page<LoanApplicationResponseDto>> response =
                loanService.getAllLoansProcessing(pageRequest, filterDto);

        // Assert: verify response and mapping
        assertEquals(200, response.getStatusCodeValue());
        Page<LoanApplicationResponseDto> dtoPage = response.getBody();
        assertThat(dtoPage.getTotalElements()).isEqualTo(1);
        LoanApplicationResponseDto dto = dtoPage.getContent().get(0);
        assertEquals(1234567L, dto.loanNumber());
        assertEquals(LoanType.CASH, dto.loanType());
        // Depending on your LoanMapper implementation, ensure the amount and other fields are mapped correctly.
        assertEquals(new BigDecimal("10000"), dto.amount());
        assertEquals("Education", dto.purposeOfLoan());
        assertEquals(new BigDecimal("2500"), dto.monthlyIncome());
        assertEquals("Permanent", dto.employmentStatus());
        assertEquals(60, dto.repaymentPeriod());
        assertEquals("+381641234567", dto.contactPhone());
        assertEquals("35123456789012345678", dto.accountNumber());
        assertEquals(Loan.InterestType.FIXED, dto.interestType());
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
