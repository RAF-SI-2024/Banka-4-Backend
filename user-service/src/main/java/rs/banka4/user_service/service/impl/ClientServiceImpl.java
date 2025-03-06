package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.config.RabbitMqConfig;
import rs.banka4.user_service.dto.*;
import rs.banka4.user_service.dto.requests.ClientContactRequest;
import rs.banka4.user_service.dto.requests.CreateClientDto;
import rs.banka4.user_service.dto.requests.UpdateClientDto;
import rs.banka4.user_service.exceptions.*;
import rs.banka4.user_service.mapper.BasicClientMapper;
import rs.banka4.user_service.mapper.ClientMapper;
import rs.banka4.user_service.mapper.ContactMapper;
import rs.banka4.user_service.models.Account;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.models.Privilege;
import rs.banka4.user_service.models.VerificationCode;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.service.abstraction.AccountService;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.MessageHelper;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final AccountService accountService;
    private final BasicClientMapper basicClientMapper;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final ClientMapper clientMapper;
    private final VerificationCodeService verificationCodeService;
    private final RabbitTemplate rabbitTemplate;
    private final EmployeeRepository employeeRepository;
    private final ContactMapper contactMapper;

    @Override
    public ResponseEntity<LoginResponseDto> login(LoginDto loginDto) {
        CustomUserDetailsService.role = "client"; // Consider refactoring this into a more robust role management system

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password())
            );
        } catch (BadCredentialsException e) {
            throw new IncorrectCredentials();
        }

        Client client = clientRepository.findByEmail(loginDto.email())
                .orElseThrow(() -> new UsernameNotFoundException(loginDto.email()));

        if (client.getPassword() == null) {
            throw new NotActivated();
        }

        String accessToken = jwtUtil.generateToken(client);
        String refreshToken = jwtUtil.generateRefreshToken(userDetailsService.loadUserByUsername(loginDto.email()), "client");

        return ResponseEntity.ok(new LoginResponseDto(accessToken, refreshToken));
    }

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
        var client = clientRepository.findById(id).orElseThrow(() -> new UserNotFound(id));;
        return ResponseEntity.ok(clientMapper.toDto(client));
    }

    @Override
    public ClientDto findClient(String id) {
        var c =clientRepository.findById(id);
        if(c.isEmpty()) throw  new ClientNotFound(id);

        return clientMapper.toDto(c.get());
    }

    @Override
    public Optional<Client> getClientByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    @Override
    public ResponseEntity<Void> createClient(CreateClientDto createClientDto) {

        if(clientRepository.existsByEmail(createClientDto.email())){
            throw new DuplicateEmail(createClientDto.email());
        }

        var clnt = clientMapper.toEntity(createClientDto);

        clientRepository.save(clnt);

        sendVerificationEmailToClient(createClientDto.firstName(),createClientDto.email());

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
        Optional<Client> clientOptional = clientRepository.findById(id);

        if (clientOptional.isEmpty()) { // TODO: do no let other users edit other users
            throw new ClientNotFound(id);
        }

        if (clientRepository.existsByEmail(updateClientDto.email()) || employeeRepository.existsByEmail(updateClientDto.email())) {
            throw new DuplicateEmail(updateClientDto.email());
        }

        Client client = clientOptional.get();
        client.setFirstName(updateClientDto.firstName());
        client.setLastName(updateClientDto.lastName());
        client.setDateOfBirth(updateClientDto.dateOfBirth());
        client.setGender(updateClientDto.gender());
        client.setEmail(updateClientDto.email());
        client.setPhone(updateClientDto.phone());
        client.setAddress(updateClientDto.address());
        clientRepository.save(client);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Page<ClientContactDto>> getAllContacts(String token, Pageable pageable) {
        String email = jwtUtil.extractUsername(token);
        Client client = clientRepository.findByEmail(email).orElseThrow(NotFound::new);

        List<ClientContactDto> contactDtos = client.getSavedContacts().stream()
                .map(contactMapper::toClientContactDto)
                .toList();

        return ResponseEntity.ok(new PageImpl<>(contactDtos, pageable, contactDtos.size()));
    }

    @Override
    public ResponseEntity<Void> createContact(String token, ClientContactRequest request) {
        String email = jwtUtil.extractUsername(token);
        Client client = clientRepository.findByEmail(email).orElseThrow(NotFound::new);

        client.getSavedContacts().add(accountService.getAccountByAccountNumber(request.accountNumber()));

        clientRepository.save(client);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public ResponseEntity<Void> deleteContact(String token, String accountNumber) {
        String email = jwtUtil.extractUsername(token);
        Client client = clientRepository.findByEmail(email).orElseThrow(NotFound::new);

        Set<Account> updatedContacts = new HashSet<>(client.getSavedContacts());
        updatedContacts.removeIf(account -> account.getAccountNumber().equals(accountNumber));
        client.setSavedContacts(updatedContacts);

        clientRepository.save(client);

        System.out.printf("Deleted contact with account number: %s \n", accountNumber);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    private void sendVerificationEmailToClient(String firstName, String email) {
        VerificationCode verificationCode = verificationCodeService.createVerificationCode(email);

        if (verificationCode == null || verificationCode.getCode() == null) {
            throw new IllegalStateException("Failed to generate verification code for email: " + email);
        }

        NotificationTransferDto message = MessageHelper.createAccountActivationMessage(email,
                firstName,
                verificationCode.getCode());

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_NAME,
                RabbitMqConfig.ROUTING_KEY,
                message
        );
    }
}
