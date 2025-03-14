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
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInfoDto;
import rs.banka4.user_service.domain.loan.db.LoanRequest;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.loan.dtos.LoanInformationDto;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.domain.user.employee.dtos.CreateEmployeeDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
uses = {CurrencyMapper.class, AccountMapper.class})
public interface LoanMapper {

    LoanMapper INSTANCE = Mappers.getMapper(LoanMapper.class);

    @Mapping(target = "currency", expression = "java(currencyMapper.toDto(loan.getAccount().getCurrency()))") //OGI N. DOKTOR
    LoanInformationDto toDto(Loan loan, @Context CurrencyMapper currencyMapper);

    @AfterMapping
    default void mapCurrency(Loan loan, @MappingTarget LoanInfoDto loanInfoDto) {
        if (loan.getAccount() != null) {
            loanInfoDto.setCurrency(CurrencyMapper.INSTANCE.toDto(loan.getAccount().getCurrency()));
        }
    }

    @Mapping(source = "loanType",target = "type")
    Loan toEntity(LoanApplicationDto loanApplicationDto);

    @Mapping(target = "currency",ignore = true)
    @Mapping(target = "type", source = "loanType")
    LoanRequest toLoanRequest(LoanApplicationDto loanApplicationDto);

//    @AfterMapping
//    default void mapCurrency(Loan loan, @MappingTarget LoanInformationDto loanInformationDto) {
//        if (loan.getAccount() != null) {
//            loanInformationDto.currency() = CurrencyMapper.INSTANCE.toDto(loan.getAccount().getCurrency());
//        }
//    }

    @Mapping(source = "loanType",target = "type")
    Loan toEntity(LoanApplicationDto loanApplicationDto);

    @Mapping(target = "currency",ignore = true)
    @Mapping(target = "type", source = "loanType")
    LoanRequest toLoanRequest(LoanApplicationDto loanApplicationDto);

}
