package rs.banka4.user_service.service.mock;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.currency.dtos.CurrencyDto;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.domain.loan.db.LoanType;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.loan.dtos.LoanFilterDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;
import rs.banka4.user_service.service.abstraction.LoanService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class LoanServiceMock implements LoanService {

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

    @Override
    public void createLoanApplication(LoanApplicationDto loanApplicationDto, String auth) {

    }

    @Override
    public ResponseEntity<Page<LoanInformationDto>> getAllLoans(PageRequest pageRequest , LoanFilterDto filterDto) {
        Page<LoanInformationDto> dtos = new PageImpl<>(mockLoans, pageRequest, mockLoans.size());
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<Page<LoanInformationDto>> getMyLoans(String token, PageRequest pageRequest) {
        Page<LoanInformationDto> dtos = new PageImpl<>(mockLoans, pageRequest, mockLoans.size());
        return ResponseEntity.ok(dtos);
    }

    @Override
    public void approveLoan(Long loanNumber, String auth) {

    }

    @Override
    public void rejectLoan(Long loanNumber, String auth) {

    }
}
