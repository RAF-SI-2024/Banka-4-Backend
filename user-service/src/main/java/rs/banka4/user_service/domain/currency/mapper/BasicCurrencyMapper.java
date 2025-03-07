package rs.banka4.user_service.domain.currency.mapper;

import rs.banka4.user_service.domain.currency.dtos.CurrencyDto;
import rs.banka4.user_service.domain.currency.db.Currency;

public class BasicCurrencyMapper {

    public CurrencyDto toDto(Currency currency){
        return new CurrencyDto(
                currency.getId().toString(),
                currency.getName(),
                currency.getSymbol(),
                currency.getDescription(),
                currency.isActive(),
                currency.getCode()
        );
    }
}
