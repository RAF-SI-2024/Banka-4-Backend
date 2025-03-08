package rs.banka4.user_service.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.user.client.db.ClientContact;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactDto;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactRequest;
import rs.banka4.user_service.domain.user.client.mapper.ClientContactMapper;
import rs.banka4.user_service.repositories.ClientContactRepository;
import rs.banka4.user_service.service.abstraction.ClientContactService;

import java.util.UUID;

@Service
public class ClientContactServiceImpl implements ClientContactService {

    private ClientContactRepository clientContactRepository;

    @Override
    public Page<ClientContactDto> getAllClientContacts(Pageable pageable) {
        Page<ClientContact> clientContacts = clientContactRepository.findAll(pageable);
        return clientContacts.map(ClientContactMapper.INSTANCE::toDto);
    }

    @Override
    public void createClientContact(ClientContactRequest request) {
        //ClientContact client = ClientContactMapper.INSTANCE.toEntity(request);
    }

    @Override
    public void updateClientContact(UUID id, ClientContactRequest request) {

    }

    @Override
    public void deleteClientContact(UUID contactId) {

    }
}
