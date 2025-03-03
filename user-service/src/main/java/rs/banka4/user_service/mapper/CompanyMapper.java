package rs.banka4.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import rs.banka4.user_service.dto.CompanyDto;
import rs.banka4.user_service.dto.requests.CreateCompanyDto;
import rs.banka4.user_service.models.Company;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyMapper {
    CompanyDto toDto(Company company);
    Company toEntity(CompanyDto dto);
    Company toEntity(CreateCompanyDto dto);
}
