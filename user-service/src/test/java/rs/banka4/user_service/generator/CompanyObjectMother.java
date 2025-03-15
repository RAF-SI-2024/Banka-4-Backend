package rs.banka4.user_service.generator;

import rs.banka4.user_service.domain.company.dtos.CompanyDto;
import rs.banka4.user_service.domain.company.dtos.CreateCompanyDto;
import rs.banka4.user_service.domain.company.db.Company;
import rs.banka4.user_service.domain.user.client.db.Client;

import java.util.UUID;

public class CompanyObjectMother {

    public static CreateCompanyDto createCompanyDto() {
        return new CreateCompanyDto(
                "Acme Corp",
                "123456789",
                "987654321",
                "123 Some Address",
                "12222"
        );

    }

    public static Company createCompanyEntity(CreateCompanyDto dto) {
        Company company = new Company();
        company.setTin(dto.tin());
        company.setCrn(dto.crn());
        company.setName(dto.name());
        company.setAddress(dto.address());
        return company;
    }

    public static CompanyDto createCompanyDtoWithId() {
        return new CompanyDto(
                UUID.randomUUID().toString(),
                "Rafovari",
                "123456789",
                "987654321",
                "123 Main St",
                "441100"
        );
    }

    public static Company createCompanyEntityWithId(Client client) {
        return new Company(
                UUID.randomUUID(),
                "Rafovari",
                "123456789",
                "987654321",
                "123 Main St",
                null,
                client
        );
    }
}
