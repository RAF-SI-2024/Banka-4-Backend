package rs.banka4.user_service.domain.loan.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.account.mapper.AccountMapper;
import rs.banka4.user_service.domain.currency.mapper.CurrencyMapper;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanInstallment;
import rs.banka4.user_service.domain.loan.db.LoanRequest;
import rs.banka4.user_service.domain.loan.dtos.*;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        CurrencyMapper.class,AccountMapper.class
    }
)
public interface LoanMapper {

    LoanMapper INSTANCE = Mappers.getMapper(LoanMapper.class);

    LoanInstallmentDto toDto(LoanInstallment loanInstallment);

    @Mapping(
        target = "currency",
        expression = "java(loan.getAccount().getCurrency().getCode())"
    )
    LoanInformationDto toDto(Loan loan);

    @Mapping(
        source = "loan.loanNumber",
        target = "loanNumber"
    )
    @Mapping(
        target = "loanType",
        source = "type"
    )
    @Mapping(
        target = "accountNumber",
        expression = "java(loanRequest.getAccount().getAccountNumber())"
    )
    @Mapping(
        target = "currency",
        expression = "java(loanRequest.getAccount().getCurrency().getCode())"
    )
    LoanApplicationResponseDto toDtoApplicationResponse(LoanRequest loanRequest);

    @Mapping(
        source = "loanType",
        target = "type"
    )
    Loan toEntity(LoanApplicationDto loanApplicationDto);

    @Mapping(
        target = "currency",
        ignore = true
    )
    @Mapping(
        target = "type",
        source = "loanType"
    )
    LoanRequest toLoanRequest(LoanApplicationDto loanApplicationDto);


}
