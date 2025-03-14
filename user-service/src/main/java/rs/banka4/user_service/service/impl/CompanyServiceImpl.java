package rs.banka4.user_service.service.impl;

import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.company.db.Company;
import rs.banka4.user_service.domain.company.dtos.CreateCompanyDto;
import rs.banka4.user_service.domain.company.mapper.CompanyMapper;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.exceptions.company.DuplicateCompanyName;
import rs.banka4.user_service.exceptions.company.DuplicateCrn;
import rs.banka4.user_service.exceptions.company.DuplicateTin;
import rs.banka4.user_service.repositories.CompanyRepository;
import rs.banka4.user_service.service.abstraction.CompanyService;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyMapper companyMapper;
    private final CompanyRepository companyRepository;

    @Override
    public ResponseEntity<Void> createCompany(@Valid CreateCompanyDto dto, Client client) {

        if (companyRepository.existsByCrn(dto.crn())) throw new DuplicateCrn(dto.crn());
        else if (companyRepository.existsByTin(dto.tin())) throw new DuplicateTin(dto.tin());
        else
            if (companyRepository.existsByName(dto.name()))
                throw new DuplicateCompanyName(dto.name());

        var comp = companyMapper.toEntity(dto);
        comp.setMajorityOwner(client);

        companyRepository.save(comp);

        return ResponseEntity.status(HttpStatus.CREATED)
            .build();
    }

    @Override
    public Optional<Company> getCompany(String id) {
        return companyRepository.findById(UUID.fromString(id));
    }

    @Override
    public Optional<Company> getCompanyByCrn(String crn) {
        return companyRepository.findByCrn(crn);
    }
}
