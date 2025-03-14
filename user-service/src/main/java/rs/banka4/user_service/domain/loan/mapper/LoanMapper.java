package rs.banka4.user_service.domain.loan.mapper;

import org.mapstruct.*;
import org.mapstruct.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.account.mapper.AccountMapper;
import rs.banka4.user_service.domain.currency.dtos.CurrencyDto;
import rs.banka4.user_service.domain.currency.mapper.CurrencyMapper;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanRequest;
import rs.banka4.user_service.domain.loan.dtos.*;
import rs.banka4.user_service.domain.loan.db.LoanRequest;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.domain.user.employee.dtos.CreateEmployeeDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
uses = {CurrencyMapper.class, AccountMapper.class})
public interface LoanMapper {

    LoanMapper INSTANCE = Mappers.getMapper(LoanMapper.class);

    @Mapping(target = "currency", expression = "java(currencyMapper.toDto(loan.getAccount().getCurrency()))")
    LoanInformationDto toDto(Loan loan, @Context CurrencyMapper currencyMapper);
    @Mapping(target = "loanNumber", expression = "java(loan.getLoan().getLoanNumber())")
    @Mapping(target = "loanType",source = "type")
    @Mapping(target = "accountNumber", expression = "java(loan.getAccount().getAccountNumber())")
    LoanApplicationResponseDto toDtoApplicationResponse(LoanRequest loan, @Context CurrencyMapper currencyMapper);


    @Mapping(source = "loanType",target = "type")
    Loan toEntity(LoanApplicationDto loanApplicationDto);

    @Mapping(target = "currency",ignore = true)
    @Mapping(target = "type", source = "loanType")
    LoanRequest toLoanRequest(LoanApplicationDto loanApplicationDto);


}
