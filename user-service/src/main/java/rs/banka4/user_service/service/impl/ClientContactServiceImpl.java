package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.db.ClientContact;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactDto;
import rs.banka4.user_service.domain.user.client.dtos.ClientContactRequest;
import rs.banka4.user_service.domain.user.client.mapper.ClientContactMapper;
import rs.banka4.user_service.exceptions.ClientContactNotFound;
import rs.banka4.user_service.exceptions.ClientNotFound;
import rs.banka4.user_service.exceptions.NotAuthenticated;
import rs.banka4.user_service.repositories.ClientContactRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.abstraction.ClientContactService;
import rs.banka4.user_service.utils.JwtUtil;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientContactServiceImpl implements ClientContactService {

    private final JwtUtil jwtUtil;
    private final ClientContactRepository clientContactRepository;
    private final ClientRepository clientRepository;

    @Override
    public Page<ClientContactDto> getAllClientContacts(String token, Pageable pageable) {
        String email = jwtUtil.extractUsername(token);
        Client client = clientRepository.findByEmail(email).orElseThrow(() -> new ClientNotFound(email));

        Page<ClientContact> clientContacts = clientContactRepository.findAllActive(pageable, client);
        return clientContacts.map(ClientContactMapper.INSTANCE::toDto);
    }

    @Override
    public ClientContactDto getSpecificClientContact(String token, UUID id) {
        String email = jwtUtil.extractUsername(token);
        Client client = clientRepository.findByEmail(email).orElseThrow(() -> new ClientNotFound(email));

        ClientContact clientContact = clientContactRepository.findById(id).orElseThrow(ClientContactNotFound::new);

        if (!clientContact.getClient().equals(client)) {
            throw new NotAuthenticated();
        }

        return ClientContactMapper.INSTANCE.toDto(clientContact);
    }

    @Override
    public void createClientContact(String token, ClientContactRequest request) {
        String email = jwtUtil.extractUsername(token);
        Client client = clientRepository.findByEmail(email).orElseThrow(() -> new ClientNotFound(email));

        ClientContact clientContact = ClientContactMapper.INSTANCE.toEntity(request);
        clientContact.setClient(client);

        clientContactRepository.save(clientContact);
    }

    @Override
    public void updateClientContact(String token, UUID id, ClientContactRequest request) {
        String email = jwtUtil.extractUsername(token);
        Client client = clientRepository.findByEmail(email).orElseThrow(() -> new ClientNotFound(email));

        ClientContact clientContact = clientContactRepository.findById(id).orElseThrow(ClientContactNotFound::new);

        if (!clientContact.getClient().equals(client)) {
            throw new NotAuthenticated();
        }

        ClientContactMapper.INSTANCE.fromUpdate(clientContact, request);
        clientContactRepository.save(clientContact);
    }

    @Override
    public void deleteClientContact(String token, UUID contactId) {
        String email = jwtUtil.extractUsername(token);
        Client client = clientRepository.findByEmail(email).orElseThrow(() -> new ClientNotFound(email));

        ClientContact clientContact = clientContactRepository.findById(contactId).orElseThrow(ClientContactNotFound::new);

        if (!clientContact.getClient().equals(client)) {
            throw new NotAuthenticated();
        }

        clientContact.setDeleted(true);
        clientContactRepository.save(clientContact);
    }
}
