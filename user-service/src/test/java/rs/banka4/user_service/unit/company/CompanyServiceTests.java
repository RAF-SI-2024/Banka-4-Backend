package rs.banka4.user_service.unit.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import rs.banka4.user_service.domain.company.dtos.CreateCompanyDto;
import rs.banka4.user_service.domain.company.db.Company;
import rs.banka4.user_service.domain.company.mapper.CompanyMapper;
import rs.banka4.user_service.exceptions.DuplicateCrn;
import rs.banka4.user_service.exceptions.DuplicateTin;
import rs.banka4.user_service.generator.CompanyObjectMother;
import rs.banka4.user_service.repositories.CompanyRepository;
import rs.banka4.user_service.service.impl.CompanyServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyServiceTests {

    @Mock
    private CompanyMapper companyMapper;
    @Mock
    private CompanyRepository companyRepository;
    @InjectMocks
    private CompanyServiceImpl companyService;

    private CreateCompanyDto createCompanyDto;
    private Company companyEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        createCompanyDto = CompanyObjectMother.createCompanyDto();
        companyEntity = CompanyObjectMother.createCompanyEntity(createCompanyDto);
    }

    @Test
    void testCreateCompanySuccess() {
        // Arrange
        when(companyRepository.existsByCrn(createCompanyDto.crn())).thenReturn(false);
        when(companyRepository.existsByTin(createCompanyDto.tin())).thenReturn(false);
        when(companyMapper.toEntity(createCompanyDto)).thenReturn(companyEntity);

        // Act
        companyService.createCompany(createCompanyDto, null);

        // Assert
        verify(companyRepository).existsByCrn(createCompanyDto.crn());
        verify(companyRepository).existsByTin(createCompanyDto.tin());
        verify(companyMapper).toEntity(createCompanyDto);

        ArgumentCaptor<Company> savedCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(savedCaptor.capture());
        Company savedCompany = savedCaptor.getValue();

        assertEquals(createCompanyDto.name(), savedCompany.getName());
        assertEquals(createCompanyDto.crn(), savedCompany.getCrn());
        assertEquals(createCompanyDto.tin(), savedCompany.getTin());
        assertEquals(createCompanyDto.address(), savedCompany.getAddress());
    }

    @Test
    void testCreateCompanyCrnAlreadyExists() {
        // Arrange
        when(companyRepository.existsByCrn(createCompanyDto.crn())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateCrn.class, () -> companyService.createCompany(createCompanyDto, null));

        verify(companyRepository, never()).existsByTin(anyString());
        verify(companyMapper, never()).toEntity(any(CreateCompanyDto.class));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void testCreateCompanyTinAlreadyExists() {
        // Arrange
        when(companyRepository.existsByCrn(createCompanyDto.crn())).thenReturn(false);
        when(companyRepository.existsByTin(createCompanyDto.tin())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateTin.class, () -> companyService.createCompany(createCompanyDto, null));

        verify(companyRepository).existsByCrn(createCompanyDto.crn());
        verify(companyRepository).existsByTin(createCompanyDto.tin());
        verify(companyMapper, never()).toEntity(any(CreateCompanyDto.class));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void testGetCompany() {
        // Arrange
        UUID companyId = UUID.fromString("35bc1ef6-f6d0-4405-bcdb-7dc0686b7b87");
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(companyEntity));

        // Act
        Optional<Company> result = companyService.getCompany(companyId.toString());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(companyEntity, result.get());
        verify(companyRepository).findById(companyId);
    }

    @Test
    void testGetCompanyByCrn() {
        // Arrange
        String crn = companyEntity.getCrn();
        when(companyRepository.findByCrn(crn)).thenReturn(Optional.of(companyEntity));

        // Act
        Optional<Company> result = companyService.getCompanyByCrn(crn);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(companyEntity, result.get());
        verify(companyRepository).findByCrn(crn);
    }

}