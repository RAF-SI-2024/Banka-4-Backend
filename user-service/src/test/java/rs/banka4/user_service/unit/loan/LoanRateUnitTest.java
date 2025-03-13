package rs.banka4.user_service.unit.loan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.banka4.user_service.domain.loan.db.BankMargin;
import rs.banka4.user_service.domain.loan.db.LoanType;
import rs.banka4.user_service.exceptions.loan.LoanTypeNotFound;
import rs.banka4.user_service.repositories.BankMarginRepository;
import rs.banka4.user_service.utils.loans.LoanRateUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanRateUtilTest {

    @Mock
    private BankMarginRepository bankMarginRepository;

    @InjectMocks
    private LoanRateUtil loanRateUtil;

    private BankMargin fixedLoanMargin;

    @BeforeEach
    void setUp() {
        fixedLoanMargin = new BankMargin();
        fixedLoanMargin.setMargin(new BigDecimal("2.5"));
    }

    @Test
    void testCalculateInterestRate_Success() {
        BigDecimal referenceValue = new BigDecimal("5.0");
        LoanType loanType = LoanType.AUTO_LOAN;

        when(bankMarginRepository.findBankMarginByType(loanType)).thenReturn(Optional.of(fixedLoanMargin));

        BigDecimal result = loanRateUtil.calculateInterestRate(referenceValue, loanType);
        assertNotNull(result);
        assertEquals(new BigDecimal("0.6"), result);
    }

    @Test
    void testCalculateInterestRate_ThrowsException_WhenLoanTypeNotFound() {
        LoanType loanType = LoanType.CASH;
        when(bankMarginRepository.findBankMarginByType(loanType)).thenReturn(Optional.empty());

        assertThrows(LoanTypeNotFound.class, () -> loanRateUtil.calculateInterestRate(BigDecimal.TEN, loanType));
    }

    @Test
    void testCalculateMonthly_FixedLoan() {
        BigDecimal loanAmount = BigDecimal.valueOf(100000);
        BigDecimal monthlyInterestRate = new BigDecimal("0.005");
        BigInteger numberOfInstallments = BigInteger.valueOf(12);

        BigDecimal result = loanRateUtil.calculateMonthly(loanAmount, monthlyInterestRate, numberOfInstallments);

        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalculateMonthly_ZeroInterest() {
        BigDecimal loanAmount = BigDecimal.valueOf(120000);
        BigDecimal monthlyInterestRate = BigDecimal.ZERO;
        BigInteger numberOfInstallments = BigInteger.valueOf(12);

        BigDecimal result = loanRateUtil.calculateMonthly(loanAmount, monthlyInterestRate, numberOfInstallments);

        assertEquals(new BigDecimal("10000.0000000000"), result);
    }
}
