package rs.banka4.user_service.generator;

import java.math.BigDecimal;
import java.time.LocalDate;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.domain.loan.db.LoanType;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;

public class LoanObjectMother {

    public static LoanApplicationDto generateLoanApplicationDto() {
        return new LoanApplicationDto(
            LoanType.CASH,
            BigDecimal.valueOf(1000.00),
            Currency.Code.RSD,
            "Education",
            BigDecimal.valueOf(2500.00),
            "Unemployed",
            5,
            36,
            "+38162000000",
            "444394438340549",
            Loan.InterestType.FIXED
        );
    }

    public static LoanInformationDto generateLoanInformationDto() {
        return new LoanInformationDto(
            12847218L,
            LoanType.CASH,
            BigDecimal.valueOf(1000.00),
            36,
            BigDecimal.valueOf(1.00),
            BigDecimal.valueOf(1.20),
            LocalDate.now(),
            LocalDate.now()
                .plusDays(300),
            BigDecimal.valueOf(100.00),
            LocalDate.now()
                .plusDays(30),
            BigDecimal.valueOf(900.00),
            Currency.Code.EUR,
            LoanStatus.APPROVED,
            Loan.InterestType.FIXED
        );
    }

}
