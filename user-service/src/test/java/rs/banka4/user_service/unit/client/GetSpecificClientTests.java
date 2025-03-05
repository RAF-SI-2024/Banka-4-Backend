package rs.banka4.user_service.unit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import rs.banka4.user_service.dto.ClientDto;
import rs.banka4.user_service.dto.requests.CreateClientDto;
import rs.banka4.user_service.exceptions.DuplicateEmail;
import rs.banka4.user_service.exceptions.UserNotFound;
import rs.banka4.user_service.mapper.ClientMapper;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.models.Privilege;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.impl.ClientServiceImpl;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetSpecificClientTests {
    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientServiceImpl clientService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldThrowExceptionWhenClientWithIdDoesntExist(){
        when(clientRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> clientService.getClient("1"));
    }
    @Test
    public void shouldFindClientWithSpecificId(){
        Client clientEntity = new Client();
        clientEntity.setId("1");
        clientEntity.setEmail("john.doe@example.com");
        clientEntity.setFirstName("John");
        clientEntity.setLastName("Doe");

        ClientDto createClientDto = new ClientDto(
                "1",
                "John",
                "Doe",
                LocalDate.now(),
                "Male",
                "john.doe@example.com",
                "12313",
                "dasasd",
                EnumSet.of(Privilege.SEARCH),
                List.of()
        );

        when(clientRepository.findById("1")).thenReturn(Optional.of(clientEntity));
        when(clientMapper.toDto(clientEntity)).thenReturn(createClientDto);

        var res = clientService.getClient(clientEntity.id);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(createClientDto, res.getBody());
    }
}
