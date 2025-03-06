package rs.banka4.user_service.unit.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import rs.banka4.user_service.dto.CompanyDto;
import rs.banka4.user_service.dto.requests.CreateCompanyDto;
import rs.banka4.user_service.exceptions.DuplicateCrn;
import rs.banka4.user_service.exceptions.DuplicateTin;
import rs.banka4.user_service.mapper.CompanyMapper;
import rs.banka4.user_service.models.Company;

import rs.banka4.user_service.repositories.CompanyRepository;
import rs.banka4.user_service.service.impl.CompanyServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyServiceCreateTests {

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void createCompanySuccessfully() {
        CreateCompanyDto dto = new CreateCompanyDto(
                "Acme Corp",
                "123456789",
                "987654321",
                "123 Some Address",
                "12222"
        );

        when(companyRepository.existsByCrn(dto.crn())).thenReturn(false);
        when(companyRepository.existsByTin(dto.tin())).thenReturn(false);

        Company companyEntity = new Company();
        companyEntity.setTin(dto.tin());
        companyEntity.setCrn(dto.crn());
        companyEntity.setName(dto.name());
        companyEntity.setAddress(dto.address());

        when(companyMapper.toEntity(dto)).thenReturn(companyEntity);

        companyService.createCompany(dto, null);

        verify(companyRepository).existsByCrn(dto.crn());
        verify(companyRepository).existsByTin(dto.tin());

        verify(companyMapper).toEntity(dto);

        ArgumentCaptor<Company> savedCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(savedCaptor.capture());
        Company savedCompany = savedCaptor.getValue();

        assertEquals(dto.name(), savedCompany.getName());
        assertEquals(dto.crn(), savedCompany.getCrn());
        assertEquals(dto.tin(), savedCompany.getTin());
        assertEquals(dto.address(), savedCompany.getAddress());
    }

    @Test
    void shouldThrowExceptionWhenCrnAlreadyExists() {
        CreateCompanyDto dto = new CreateCompanyDto(
                "Acme Corp",
                "123456789",
                "987654321",
                "123 Some Address"
                , "12222"
        );

        when(companyRepository.existsByCrn(dto.crn())).thenReturn(true);

        assertThrows(DuplicateCrn.class, () -> companyService.createCompany(dto, null));

        verify(companyRepository, never()).existsByTin(anyString());

        verify(companyMapper, never()).toEntity(any(CompanyDto.class));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenTinAlreadyExists() {
        CreateCompanyDto dto = new CreateCompanyDto(
                "Acme Corp",
                "123456789",
                "987654321",
                "123 Some Address"
                , "12222"
        );

        when(companyRepository.existsByCrn(dto.crn())).thenReturn(false);
        when(companyRepository.existsByTin(dto.tin())).thenReturn(true);

        assertThrows(DuplicateTin.class, () -> companyService.createCompany(dto, null));

        verify(companyRepository).existsByCrn(dto.crn());
        verify(companyRepository).existsByTin(dto.tin());
        verify(companyMapper, never()).toEntity(any(CompanyDto.class));
        verify(companyRepository, never()).save(any());
    }
}
