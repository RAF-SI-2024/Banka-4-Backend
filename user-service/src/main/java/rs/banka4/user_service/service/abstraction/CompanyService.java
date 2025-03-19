package rs.banka4.user_service.service.abstraction;

import java.util.Optional;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.company.db.Company;
import rs.banka4.user_service.domain.company.dtos.CreateCompanyDto;
import rs.banka4.user_service.domain.user.client.db.Client;

public interface CompanyService {
    ResponseEntity<Void> createCompany(CreateCompanyDto dto, Client client);

    Optional<Company> getCompany(String id);

    Optional<Company> getCompanyByCrn(String crn);
}
