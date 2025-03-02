package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.ClientDto;
import rs.banka4.user_service.dto.requests.CreateClientDto;
import rs.banka4.user_service.dto.PrivilegesDto;
import rs.banka4.user_service.dto.requests.UpdateClientDto;

public interface ClientService {
    ResponseEntity<PrivilegesDto> getPrivileges(String token);
    ResponseEntity<ClientDto> getMe(String token);
    ResponseEntity<ClientDto> getClient(String id);
    ResponseEntity<Void> createClient(CreateClientDto createClientDto);
    ResponseEntity<Page<ClientDto>> getClients(String firstName, String lastName, String email, String phone, Pageable pageable);
    ResponseEntity<Void> updateClient(String id, UpdateClientDto updateClientDto);
}
