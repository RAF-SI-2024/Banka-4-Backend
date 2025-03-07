//package rs.banka4.user_service.unit;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentMatchers;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.*;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
//import rs.banka4.user_service.exceptions.NonexistantSortByField;
//import rs.banka4.user_service.exceptions.NullPageRequest;
//import rs.banka4.user_service.domain.user.client.mapper.BasicClientMapperForGetAll;
//import rs.banka4.user_service.domain.user.client.mapper.ClientMapper;
//import rs.banka4.user_service.mapper.ClientMapperImpl;
//import rs.banka4.user_service.domain.account.db.Account;
//import rs.banka4.user_service.domain.user.client.db.Client;
//import rs.banka4.user_service.repositories.ClientRepository;
//import rs.banka4.user_service.service.impl.ClientServiceImpl;
//
//import java.time.LocalDate;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class ClientServiceTest {
//
//    @Mock
//    private ClientRepository clientRepository;
//
//    @InjectMocks
//    private BasicClientMapperForGetAll basicClientMapperForGetAll;
//
//    @InjectMocks
//    private ClientServiceImpl clientService;
//
//
//    private Client client;
//    private PageRequest pageRequest;
//
//    List<Client> sortedByEmail;
//    List<Client> sortedByName;
//    List<Client> sortedByLastName;
//
//    @BeforeEach
//    void setUp() {
//        pageRequest = PageRequest.of(0, 10);
//
//        Set<String> linkedAccounts = new HashSet<>();
//        linkedAccounts.add("265000000000123456");
//
//        client = new Client();
//        client.setId("1");
//        client.setFirstName("Djovak");
//        client.setLastName("Nokovic");
//        client.setDateOfBirth(LocalDate.of(1990, 5, 15));
//        client.setGender("Male");
//        client.setEmail("djovaknokovic@example.com");
//        client.setPhone("+1234567890");
//        client.setAddress("123 Grove Street, City, Country");
//        client.setEnabled(true);
//        client.setAccounts(new HashSet<Account>());
//        clientRepository.save(client);
//        Client client1 = client;
//
//        client = new Client();
//        client.setId("2");
//        client.setFirstName("Aleksa");
//        client.setLastName("Zubic");
//        client.setDateOfBirth(LocalDate.of(1990, 5, 15));
//        client.setGender("Male");
//        client.setEmail("azubic@example.com");
//        client.setPhone("+5554567890");
//        client.setAddress("124 Grove Street, City, Country");
//        client.setEnabled(true);
//        client.setAccounts(new HashSet<Account>());
//        clientRepository.save(client);
//        Client client2 = client;
//
//        client = new Client();
//        client.setId("2");
//        client.setFirstName("Zorana");
//        client.setLastName("Aleksic");
//        client.setDateOfBirth(LocalDate.of(1990, 5, 15));
//        client.setGender("Female");
//        client.setEmail("zaleksic@example.com");
//        client.setPhone("+6664567890");
//        client.setAddress("124 Grove Street, City, Country");
//        client.setEnabled(true);
//        client.setAccounts(new HashSet<Account>());
//        Client client3 = client;
//
//        sortedByEmail = List.of(client2, client1, client3);
//        sortedByName = List.of(client2, client1, client3);
//        sortedByLastName = List.of(client3, client1, client1);
//
//        clientRepository.save(client);
//    }
//
//    @Test
//    void testGetAllWithFilters() {
//        Page<Client> clientPage = new PageImpl<>(Collections.singletonList(client), pageRequest, 1);
//        PageRequest pageRequestWithSort = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), Sort.by("firstName"));
//        when(clientRepository.findAll(ArgumentMatchers.<Specification<Client>>any(), eq(pageRequestWithSort)))
//                .thenReturn(clientPage);
//
//        ClientDto expectedDto = basicClientMapperForGetAll.toDto(client);
//
//        ResponseEntity<Page<ClientDto>> response = clientService.getAll("Djovak", "Nokovic", "djovaknokovic@example.com", "firstName", pageRequest);
//
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals(1, response.getBody().getTotalElements());
//        ClientDto resultDto = response.getBody().getContent().get(0);
//        assertEquals(expectedDto.firstName(), resultDto.firstName());
//        assertEquals(expectedDto.lastName(), resultDto.lastName());
//        assertEquals(expectedDto.email(), resultDto.email());
//    }
//
//    @Test
//    void testGetAllWithNoFilters() {
//        Page<Client> clientPage = new PageImpl<>(Collections.singletonList(client), pageRequest, 1);
//        PageRequest pageRequestWithSort = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), Sort.by("firstName"));
//
//        when(clientRepository.findAll(ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Client>>any(), eq(pageRequestWithSort)))
//                .thenReturn(clientPage);
//
//        ClientDto expectedDto = basicClientMapperForGetAll.toDto(client);
//        ResponseEntity<Page<ClientDto>> response = clientService.getAll(null, null, null, null, pageRequest);
//
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals(1, response.getBody().getTotalElements());
//        ClientDto resultDto = response.getBody().getContent().get(0);
//        assertEquals(expectedDto.firstName(), resultDto.firstName());
//    }
//
//    @Test
//    void testGetAllWithInvalidSort() {
//        assertThrows(NonexistantSortByField.class, () ->
//                clientService.getAll("Djovak", "Nokovic", "djovaknokovic@example.com", "invalidSort", pageRequest)
//        );
//    }
//
//    @Test
//    void testGetAllWithEmptyPage() {
//        Page<Client> emptyPage = new PageImpl<>(Collections.emptyList(), pageRequest, 0);
//        PageRequest pageRequestWithSort = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), Sort.by("firstName"));
//
//        when(clientRepository.findAll(ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Client>>any(), eq(pageRequestWithSort)))
//                .thenReturn(emptyPage);
//
//        ResponseEntity<Page<ClientDto>> response = clientService.getAll(null, null, null, null, pageRequest);
//
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertTrue(response.getBody().getContent().isEmpty());
//    }
//
//    @Test
//    void testGetAllWithNullPageRequestThrowsException() {
//        assertThrows(NullPageRequest.class, () ->
//                clientService.getAll("Djovak", "Nokovic", "djovaknokovic@example.com", "firstName", null)
//        );
//    }
//
//    @Test
//    void testGetAllWithNonexistentFiltersReturnsEmpty() {
//        PageRequest pageRequestWithSort = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), Sort.by("firstName"));
//        Page<Client> emptyPage = new PageImpl<>(Collections.emptyList(), pageRequestWithSort, 0);
//        when(clientRepository.findAll(ArgumentMatchers.<Specification<Client>>any(), eq(pageRequestWithSort)))
//                .thenReturn(emptyPage);
//
//        ResponseEntity<Page<ClientDto>> response = clientService.getAll("Nonexistent", "Filter", "noemail@example.com", "firstName", pageRequest);
//
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals(0, response.getBody().getTotalElements());
//        assertTrue(response.getBody().getContent().isEmpty());
//    }
//
//    @Test
//    void testGetAllWithPartialFiltersReturningEmpty() {
//        PageRequest pageRequestWithSort = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), Sort.by("firstName"));
//        Page<Client> emptyPage = new PageImpl<>(Collections.emptyList(), pageRequestWithSort, 0);
//        when(clientRepository.findAll(ArgumentMatchers.<Specification<Client>>any(), eq(pageRequestWithSort)))
//                .thenReturn(emptyPage);
//
//        ResponseEntity<Page<ClientDto>> response = clientService.getAll(null, null, "nomatch@example.com", "firstName", pageRequest);
//
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals(0, response.getBody().getTotalElements());
//        assertTrue(response.getBody().getContent().isEmpty());
//    }
//
//    @Test
//    void testSortByEmail() {
//        Page<Client> clientPage = new PageImpl<>(sortedByEmail, pageRequest, 3);
//        PageRequest pageRequestWithSort = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), Sort.by("email"));
//        when(clientRepository.findAll(ArgumentMatchers.<Specification<Client>>any(), eq(pageRequestWithSort)))
//                .thenReturn(clientPage);
//
//        ClientDto expectedDto = basicClientMapperForGetAll.toDto(client);
//
//        ResponseEntity<Page<ClientDto>> response = clientService.getAll(null, null, null, "email", pageRequest);
//
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals(3, response.getBody().getTotalElements());
//        //ClientDto resultDto = response.getBody().getContent().get(0);
//        //assertEquals(expectedDto.email(), resultDto.email());
//        List<ClientDto> sortedDtosByEmail = sortedByEmail.stream()
//                .map(basicClientMapperForGetAll::toDto).toList();
//        assertEquals(response.getBody().getContent(), sortedDtosByEmail);
//    }
//
//    @Test
//    void testSortByFirstName() {
//        Page<Client> clientPage = new PageImpl<>(sortedByName, pageRequest, 3);
//        PageRequest pageRequestWithSort = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), Sort.by("firstName"));
//        when(clientRepository.findAll(ArgumentMatchers.<Specification<Client>>any(), eq(pageRequestWithSort)))
//                .thenReturn(clientPage);
//
//        ClientDto expectedDto = basicClientMapperForGetAll.toDto(client);
//
//        ResponseEntity<Page<ClientDto>> response = clientService.getAll(null, null, null, "firstName", pageRequest);
//
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals(3, response.getBody().getTotalElements());
//        //ClientDto resultDto = response.getBody().getContent().get(0);
//        //assertEquals(expectedDto.email(), resultDto.email());
//        List<ClientDto> sortedDtosName = sortedByName.stream()
//                .map(basicClientMapperForGetAll::toDto).toList();
//        assertEquals(response.getBody().getContent(), sortedDtosName);
//    }
//
//    @Test
//    void testSortByLastName() {
//        Page<Client> clientPage = new PageImpl<>(sortedByLastName, pageRequest, 3);
//        PageRequest pageRequestWithSort = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), Sort.by("lastName"));
//        when(clientRepository.findAll(ArgumentMatchers.<Specification<Client>>any(), eq(pageRequestWithSort)))
//                .thenReturn(clientPage);
//
//        ClientDto expectedDto = basicClientMapperForGetAll.toDto(client);
//
//        ResponseEntity<Page<ClientDto>> response = clientService.getAll(null, null, null, "lastName", pageRequest);
//
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals(3, response.getBody().getTotalElements());
//        //ClientDto resultDto = response.getBody().getContent().get(0);
//        //assertEquals(expectedDto.email(), resultDto.email());
//        List<ClientDto> sortedDtosByLastName = sortedByLastName.stream()
//                .map(basicClientMapperForGetAll::toDto).toList();
//        assertEquals(response.getBody().getContent(), sortedDtosByLastName);
//    }
//
//}