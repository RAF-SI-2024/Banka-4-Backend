package rs.banka4.user_service.domain.company.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import rs.banka4.user_service.domain.company.dtos.CompanyDto;
import rs.banka4.user_service.domain.company.dtos.CreateCompanyDto;
import rs.banka4.user_service.domain.company.db.ActivityCode;
import rs.banka4.user_service.domain.company.db.Company;
import rs.banka4.user_service.repositories.ActivityCodeRepository;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CompanyMapper {

    @Autowired
    private ActivityCodeRepository activityCodeRepository;

    public abstract CompanyDto toDto(Company company);
    public abstract CreateCompanyDto toCreateDto(CompanyDto companyDto);
    public abstract Company toEntity(CompanyDto dto);
    public abstract Company toEntity(CreateCompanyDto dto);

    public String activityCodeToString(ActivityCode activityCode) {
        return activityCode != null ? activityCode.getCode() : null;
    }

    public ActivityCode stringToActivityCode(String activityCode) {
        if (activityCode == null) {
            return null;
        }

        return activityCodeRepository.findActivityCodeByCode(activityCode)
                .orElseGet(() -> {
                    ActivityCode newActivityCode = new ActivityCode();
                    newActivityCode.setCode(activityCode);
                    newActivityCode.setSector("");
                    newActivityCode.setBranch("");
                    return activityCodeRepository.save(newActivityCode);
                });
    }
}