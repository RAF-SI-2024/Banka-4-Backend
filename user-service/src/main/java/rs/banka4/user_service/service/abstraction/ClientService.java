package rs.banka4.user_service.service.abstraction;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.domain.account.dtos.AccountClientIdDto;
import rs.banka4.user_service.domain.auth.dtos.LoginDto;
import rs.banka4.user_service.domain.auth.dtos.LoginResponseDto;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.dtos.*;

public interface ClientService {
    LoginResponseDto login(LoginDto loginDto);

    ClientDto getMe(String token);

    ClientDto getClientById(UUID id);

    Optional<Client> getClientByEmail(String email);

    void createClient(CreateClientDto createClientDto);

    Client createClient(AccountClientIdDto request);

    ResponseEntity<Page<ClientDto>> getClients(
        String firstName,
        String lastName,
        String email,
        String phone,
        String sortBy,
        PageRequest pageRequest
    );

    void updateClient(UUID id, UpdateClientDto updateClientDto);

    void activateClientAccount(Client client, String password);
}
