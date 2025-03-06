package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.dto.requests.ClientContactRequest;
import rs.banka4.user_service.dto.requests.CreateClientDto;
import rs.banka4.user_service.dto.requests.UpdateClientDto;
import rs.banka4.user_service.models.Client;

import java.util.Optional;

public interface ClientService {
    ResponseEntity<LoginResponseDto> login(LoginDto loginDto);
    ResponseEntity<PrivilegesDto> getPrivileges(String token);
    ResponseEntity<ClientDto> getMe(String token);
    ResponseEntity<ClientDto> getClient(String id);
    ClientDto findClient(String id);
    Optional<Client> getClientByEmail(String email);
    ResponseEntity<Void> createClient(CreateClientDto createClientDto);
    ResponseEntity<Page<ClientDto>> getClients(String firstName, String lastName, String email, String phone, Pageable pageable);
    ResponseEntity<Void> updateClient(String id, UpdateClientDto updateClientDto);
    ResponseEntity<Page<ClientContactDto>> getAllContacts(String token, Pageable pageable);
    ResponseEntity<Void> createContact(String token, ClientContactRequest request);
    ResponseEntity<Void> deleteContact(String token, String id);
}
