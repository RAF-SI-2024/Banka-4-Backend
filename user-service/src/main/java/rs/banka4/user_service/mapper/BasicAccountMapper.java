package rs.banka4.user_service.mapper;

import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.stereotype.Component;
import rs.banka4.user_service.dto.AccountDto;
import rs.banka4.user_service.dto.AccountTypeDto;
import rs.banka4.user_service.models.Account;
import rs.banka4.user_service.models.AccountType;
import rs.banka4.user_service.models.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class BasicAccountMapper {

    private BasicEmployeeMapper employeeMapper;
    private BasicClientMapper clientMapper;
    private BasicCompanyMapper basicCompanyMapper;
    private BasicCurrencyMapper basicCurrencyMapper;

    public AccountDto toDto(Account account){
        return new AccountDto(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getAvailableBalance(),
                account.getAccountMaintenance(),
                account.getCreatedDate(),
                account.getExpirationDate(),
                account.isActive(),
                toAccountTypeDto(account),
                account.getDailyLimit(),
                account.getMonthlyLimit(),
                basicCurrencyMapper.toDto(account.getCurrency()),
                employeeMapper.toDto(account.getEmployee()),
                clientMapper.entityToDto(account.getClient()),
                basicCompanyMapper.toDto(account.getCompany())
        );
    }

    AccountTypeDto toAccountTypeDto(Account account){
        if(account.getCurrency().getCode() == Currency.Code.RSD){
            if(account.getAccountType().isBusiness())
                return AccountTypeDto.CheckingBusiness;
            return AccountTypeDto.CheckingPersonal;
        }
        if(account.getAccountType().isBusiness())
            return AccountTypeDto.FxBusiness;
        return AccountTypeDto.FxPersonal;
    }
}
