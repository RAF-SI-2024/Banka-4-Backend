package rs.banka4.user_service.domain.account.mapper;

import org.mapstruct.*;
import rs.banka4.user_service.domain.account.dtos.AccountDto;
import rs.banka4.user_service.domain.account.dtos.AccountTypeDto;
import rs.banka4.user_service.domain.account.dtos.CreateAccountDto;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.user.client.mapper.ClientMapper;
import rs.banka4.user_service.domain.company.mapper.CompanyMapper;
import rs.banka4.user_service.domain.currency.mapper.CurrencyMapper;
import rs.banka4.user_service.domain.user.employee.mapper.EmployeeMapper;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CompanyMapper.class, CurrencyMapper.class, ClientMapper.class, EmployeeMapper.class}
)
public interface AccountMapper {
    @Mapping(target = "accountType", expression = "java(toAccountTypeDto(account))")
    AccountDto toDto(Account account);
    Account toEntity(CreateAccountDto dto);
    @Named("toAccountTypeDto")
    default AccountTypeDto toAccountTypeDto(Account account) {
        if (account == null || account.getCurrency() == null || account.getAccountType() == null) {
            return null;
        }
        if (account.getCurrency().getCode() == Currency.Code.RSD) {
            if (account.getAccountType().isBusiness()) {
                return AccountTypeDto.CheckingBusiness;
            } else {
                return AccountTypeDto.CheckingPersonal;
            }
        } else {
            if (account.getAccountType().isBusiness()) {
                return AccountTypeDto.FxBusiness;
            } else {
                return AccountTypeDto.FxPersonal;
            }
        }
    }
}
