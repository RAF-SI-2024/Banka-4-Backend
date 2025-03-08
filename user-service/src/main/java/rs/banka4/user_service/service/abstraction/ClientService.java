package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.auth.dtos.LoginDto;
import rs.banka4.user_service.domain.auth.dtos.LoginResponseDto;
import rs.banka4.user_service.domain.user.PrivilegesDto;
import rs.banka4.user_service.domain.user.client.dtos.*;
import rs.banka4.user_service.domain.user.client.db.Client;

import java.util.Optional;
import java.util.UUID;

public interface ClientService {
    LoginResponseDto login(LoginDto loginDto);
    ResponseEntity<PrivilegesDto> getPrivileges(String token);
    ClientDto getMe(String token);
    ClientDto getClientById(UUID id);
    Optional<Client> getClientByEmail(String email);
    void createClient(CreateClientDto createClientDto);
    ResponseEntity<Page<ClientDto>> getClients(String firstName, String lastName, String email, String sortBy, PageRequest pageRequest);
    void updateClient(UUID id, UpdateClientDto updateClientDto);
    void activateClientAccount(Client client, String password);
}
