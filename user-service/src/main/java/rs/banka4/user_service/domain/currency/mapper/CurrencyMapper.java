package rs.banka4.user_service.domain.currency.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.currency.dtos.CurrencyDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CurrencyMapper {

    CurrencyMapper INSTANCE = Mappers.getMapper(CurrencyMapper.class);

    CurrencyDto toDto(Currency currency);

    Currency toEntity(CurrencyDto dto);
}
