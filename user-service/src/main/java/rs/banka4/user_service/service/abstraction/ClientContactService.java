package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactDto;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactRequest;

import java.util.List;
import java.util.UUID;

public interface ClientContactService {
    Page<ClientContactDto> getAllClientContacts(String token, Pageable pageable);
    List<ClientContactDto> getClientContacts(String token);
    ClientContactDto getSpecificClientContact(String token, UUID id);
    void createClientContact(String token, ClientContactRequest request);
    void updateClientContact(String token, UUID id, ClientContactRequest request);
    void deleteClientContact(String token, UUID contactId);
}
