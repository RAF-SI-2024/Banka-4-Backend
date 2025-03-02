package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.dto.requests.CreateClientDto;
import rs.banka4.user_service.dto.requests.UpdateClientDto;
import rs.banka4.user_service.exceptions.NotAuthenticated;
import rs.banka4.user_service.exceptions.NotFound;
import rs.banka4.user_service.mapper.BasicClientMapper;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.models.Privilege;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.utils.JwtUtil;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final BasicClientMapper basicClientMapper;
    private final JwtUtil jwtUtil;


    @Override
    public ResponseEntity<PrivilegesDto> getPrivileges(String token) {
        return null;
    }

    @Override
    public ResponseEntity<ClientDto> getMe(String authorization) {
        String token = authorization.replace("Bearer ", "");
        String clientUsername = jwtUtil.extractUsername(token);

        if(jwtUtil.isTokenExpired(token)) throw new NotAuthenticated();
        if(jwtUtil.isTokenInvalidated(token)) throw new NotAuthenticated();

        Client client = clientRepository.findByEmail(clientUsername).orElseThrow(NotFound::new);

        ClientDto response = basicClientMapper.entityToDto(client);
        return ResponseEntity.ok(response);

    }

    @Override
    public ResponseEntity<ClientDto> getClient(String id) {
        ClientDto clientDto = new ClientDto(
                id,
                "MockFirstName",
                "MockLastName",
                LocalDate.of(1985, 5, 20),
                "Male",
                "mock.email@example.com",
                "987-654-3210",
                "123 Mockingbird Lane",
                EnumSet.noneOf(Privilege.class),
                List.of()
        );
        return ResponseEntity.ok(clientDto);
    }

    @Override
    public ResponseEntity<Void> createClient(CreateClientDto createClientDto) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @Override
    public ResponseEntity<Page<ClientDto>> getClients(String firstName, String lastName, String email, String phone, Pageable pageable) {
        ClientDto clientDto = new ClientDto(
                UUID.randomUUID().toString(),
                "MockedFirstName",
                "MockedLastName",
                LocalDate.of(1980, 3, 15),
                "Female",
                "mocked@example.com",
                "123-123-1234",
                "456 Mock Avenue",
                EnumSet.noneOf(Privilege.class),
                List.of()
        );
        Page<ClientDto> page = new PageImpl<>(List.of(clientDto), pageable, 1);
        return ResponseEntity.ok(page);
    }

    @Override
    public ResponseEntity<Void> updateClient(String id, UpdateClientDto updateClientDto) {
        return null;
    }
}
