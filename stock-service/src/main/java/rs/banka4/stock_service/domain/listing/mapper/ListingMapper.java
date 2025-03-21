package rs.banka4.stock_service.domain.listing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import rs.banka4.stock_service.domain.listing.db.Listing;
import rs.banka4.stock_service.domain.listing.dtos.ListingDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ListingMapper {

    ListingMapper INSTANCE = Mappers.getMapper(ListingMapper.class);

    Listing toEntity(ListingDto dto);

    ListingDto toDto(Listing listing);

}
