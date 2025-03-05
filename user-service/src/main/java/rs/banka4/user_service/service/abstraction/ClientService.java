package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.dto.requests.ClientContactRequest;
import rs.banka4.user_service.dto.requests.CreateClientDto;
import rs.banka4.user_service.dto.requests.UpdateClientDto;

public interface ClientService {
    ResponseEntity<LoginResponseDto> login(LoginDto loginDto);
    ResponseEntity<PrivilegesDto> getPrivileges(String token);
    ResponseEntity<ClientDto> getMe(String token);
    ResponseEntity<ClientDto> getClient(String id);
    ResponseEntity<Void> createClient(CreateClientDto createClientDto);
    ResponseEntity<Page<ClientDto>> getClients(String firstName, String lastName, String email, String phone, Pageable pageable);
    ResponseEntity<Void> updateClient(String id, UpdateClientDto updateClientDto);
    ResponseEntity<Page<ClientContactDto>> getAllContacts(Pageable pageable);
    ResponseEntity<Void> createContact(ClientContactRequest request);
    ResponseEntity<Void> deleteContact(String id);
}
