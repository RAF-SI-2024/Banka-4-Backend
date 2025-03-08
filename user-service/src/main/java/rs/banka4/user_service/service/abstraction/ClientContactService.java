package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactDto;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactRequest;

import java.util.UUID;

public interface ClientContactService {
    Page<ClientContactDto> getAllClientContacts(Pageable pageable);
    void createClientContact(ClientContactRequest request);
    void updateClientContact(UUID id, ClientContactRequest request);
    void deleteClientContact(UUID contactId);
}
