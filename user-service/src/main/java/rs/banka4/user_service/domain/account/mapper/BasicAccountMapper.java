package rs.banka4.user_service.domain.account.mapper;

import org.springframework.stereotype.Component;
import rs.banka4.user_service.domain.account.dtos.AccountDto;
import rs.banka4.user_service.domain.account.dtos.AccountTypeDto;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.user.client.mapper.BasicClientMapper;
import rs.banka4.user_service.domain.company.mapper.BasicCompanyMapper;
import rs.banka4.user_service.domain.currency.mapper.BasicCurrencyMapper;
import rs.banka4.user_service.domain.user.employee.mapper.BasicEmployeeMapper;

@Component
public class BasicAccountMapper {

    private BasicEmployeeMapper employeeMapper;
    private BasicClientMapper clientMapper;
    private BasicCompanyMapper basicCompanyMapper;
    private BasicCurrencyMapper basicCurrencyMapper;

    public AccountDto toDto(Account account){
        return new AccountDto(
                account.getId().toString(),
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
