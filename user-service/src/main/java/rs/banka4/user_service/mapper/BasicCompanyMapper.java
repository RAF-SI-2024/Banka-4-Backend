package rs.banka4.user_service.mapper;

import org.springframework.stereotype.Component;
import rs.banka4.user_service.dto.CompanyDto;
import rs.banka4.user_service.models.Company;

@Component
public class BasicCompanyMapper {

    CompanyDto toDto(Company company){
        if(company == null) return null;
        return new CompanyDto(
                company.getId(),
                company.getName(),
                company.getTin(),
                company.getCrn(),
                company.getAddress()
        );
    }
}
