package rs.banka4.user_service.mapper;

import org.mapstruct.*;
import rs.banka4.user_service.dto.AccountDto;
import rs.banka4.user_service.dto.AccountTypeDto;
import rs.banka4.user_service.dto.requests.CreateAccountDto;
import rs.banka4.user_service.models.Account;
import rs.banka4.user_service.models.Currency;

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
