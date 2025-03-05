package rs.banka4.user_service.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import rs.banka4.user_service.dto.CurrencyDto;
import rs.banka4.user_service.models.Currency;
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CurrencyMapper {
    CurrencyDto toDto(Currency currency);
    Currency toEntity(CurrencyDto dto);
}