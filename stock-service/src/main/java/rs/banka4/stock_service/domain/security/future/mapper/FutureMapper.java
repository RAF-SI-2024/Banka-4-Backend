package rs.banka4.stock_service.domain.security.future.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import rs.banka4.stock_service.domain.security.future.db.Future;
import rs.banka4.stock_service.domain.security.future.dtos.FutureDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FutureMapper {

    FutureMapper INSTANCE = Mappers.getMapper(FutureMapper.class);

    Future toEntity(FutureDto dto);

    FutureDto toDto(Future future);
}
