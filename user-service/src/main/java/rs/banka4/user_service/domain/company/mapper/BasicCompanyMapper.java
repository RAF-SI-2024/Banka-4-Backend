package rs.banka4.user_service.domain.company.mapper;

import org.springframework.stereotype.Component;
import rs.banka4.user_service.domain.company.dtos.CompanyDto;
import rs.banka4.user_service.domain.company.db.Company;
import rs.banka4.user_service.service.impl.CompanyServiceImpl;

@Component
public class BasicCompanyMapper {

    private final CompanyMapper companyMapper;
    private final CompanyServiceImpl companyServiceImpl;

    public BasicCompanyMapper(CompanyMapper companyMapper, CompanyServiceImpl companyServiceImpl) {
        this.companyMapper = companyMapper;
        this.companyServiceImpl = companyServiceImpl;
    }

    public CompanyDto toDto(Company company){
        if(company == null) return null;
        return new CompanyDto(
                company.getId().toString(),
                company.getName(),
                company.getTin(),
                company.getCrn(),
                company.getAddress(),
                company.getActivityCode().getCode()
        );
    }
}
