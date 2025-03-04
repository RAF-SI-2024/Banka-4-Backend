package rs.banka4.user_service.mapper;

import rs.banka4.user_service.dto.CurrencyDto;
import rs.banka4.user_service.models.Currency;

public class BasicCurrencyMapper {

    public CurrencyDto toDto(Currency currency){
        return new CurrencyDto(
                currency.getId(),
                currency.getName(),
                currency.getSymbol(),
                currency.getDescription(),
                currency.isActive(),
                currency.getCode(),
                currency.getCountries()
        );
    }
}
