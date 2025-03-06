package rs.banka4.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import rs.banka4.user_service.dto.CompanyDto;
import rs.banka4.user_service.dto.requests.CreateCompanyDto;
import rs.banka4.user_service.models.ActivityCode;
import rs.banka4.user_service.models.Company;
import rs.banka4.user_service.repositories.ActivityCodeRepository;

import java.util.UUID;

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