package rs.banka4.stock_service.domain.security.forex.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.forex.dtos.ForexPairDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ForexPairMapper {

    ForexPairMapper INSTANCE = Mappers.getMapper(ForexPairMapper.class);

    ForexPair toEntity(ForexPairDto dto);

    ForexPairDto toDto(ForexPair forexPair);
}
