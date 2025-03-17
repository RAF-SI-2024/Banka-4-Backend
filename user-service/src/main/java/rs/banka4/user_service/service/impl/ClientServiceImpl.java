package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.account.dtos.AccountClientIdDto;
import rs.banka4.user_service.domain.auth.dtos.LoginDto;
import rs.banka4.user_service.domain.auth.dtos.LoginResponseDto;
import rs.banka4.user_service.domain.authenticator.db.UserTotpSecret;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.dtos.*;
import rs.banka4.user_service.exceptions.*;
import rs.banka4.user_service.domain.user.client.mapper.ClientMapper;
import rs.banka4.user_service.exceptions.user.DuplicateEmail;
import rs.banka4.user_service.exceptions.user.IncorrectCredentials;
import rs.banka4.user_service.exceptions.user.NotAuthenticated;
import rs.banka4.user_service.exceptions.user.NotFound;
import rs.banka4.user_service.exceptions.user.client.ClientNotFound;
import rs.banka4.user_service.exceptions.user.client.NonexistantSortByField;
import rs.banka4.user_service.exceptions.user.client.NotActivated;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.UserTotpSecretRepository;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.specification.ClientSpecification;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final UserService userService;
    private final ClientRepository clientRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserTotpSecretRepository userTotpSecretRepository;

    @Override
    public ResponseEntity<Page<ClientDto>> getClients(String firstName, String lastName, String email, String phone,
                                                      String sortBy, PageRequest pageRequest) {
        if (pageRequest == null) {
            throw new NullPageRequest();
        }

        SpecificationCombinator<Client> combinator = new SpecificationCombinator<>();

        if (firstName != null && !firstName.isEmpty()) {
            combinator.and(ClientSpecification.hasFirstName(firstName));
        }
        if (lastName != null && !lastName.isEmpty()) {
            combinator.and(ClientSpecification.hasLastName(lastName));
        }
        if (email != null && !email.isEmpty()) {
            combinator.and(ClientSpecification.hasEmail(email));
        }
        if (phone != null && !phone.isEmpty()) {
            combinator.and(ClientSpecification.hasPhone(phone));
        }

        Sort sort;
        if (sortBy == null || sortBy.isEmpty() || "default".equalsIgnoreCase(sortBy)
                || "firstName".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("firstName");
        } else if ("lastName".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("lastName");
        } else if ("email".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("email");
        } else {
            throw new NonexistantSortByField(sortBy);
        }

        PageRequest pageRequestWithSort = PageRequest.of(pageRequest.getPageNumber(),
                pageRequest.getPageSize(),
                sort);

        Page<Client> clients = clientRepository.findAll(combinator.build(), pageRequestWithSort);
        Page<ClientDto> dtos = clients.map(ClientMapper.INSTANCE::toDto);
        return ResponseEntity.ok(dtos);
    }

    @Override
    public LoginResponseDto login(LoginDto loginDto) {
        CustomUserDetailsService.role = "client"; // Consider refactoring this into a more robust role management system

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password()));
        } catch (BadCredentialsException e) {
            throw new IncorrectCredentials();
        }

        Client client = clientRepository.findByEmail(loginDto.email()).orElseThrow(() -> new UsernameNotFoundException(loginDto.email()));
        if (client.getPassword() == null) {
            throw new NotActivated();
        }

        String accessToken = jwtUtil.generateToken(client);
        String refreshToken = jwtUtil.generateRefreshToken(userDetailsService.loadUserByUsername(loginDto.email()), "client");

        return new LoginResponseDto(accessToken, refreshToken);
    }

    @Override
    public ClientDto getMe(String authorization) {
        String token = authorization.replace("Bearer ", "");
        String clientEmail = jwtUtil.extractUsername(token);

        if(jwtUtil.isTokenExpired(token)) throw new NotAuthenticated();

        Optional<UserTotpSecret> userTotpSecret = userTotpSecretRepository.findByClient_Email(clientEmail);
        boolean has2FA = userTotpSecret.map(UserTotpSecret::getIsActive).orElse(false);

        Client client = clientRepository.findByEmail(clientEmail).orElseThrow(NotFound::new);

        return ClientMapper.INSTANCE.toDto(client, has2FA);
    }

    @Override
    public ClientDto getClientById(UUID id) {
        Client client = clientRepository.findById(id).orElseThrow(NotFound::new);
        boolean has2FA = userTotpSecretRepository.findByClient_Id(id)
                .map(UserTotpSecret::getIsActive)
                .orElse(false);

        return ClientMapper.INSTANCE.toDto(client, has2FA);
    }

    @Override
    public Optional<Client> getClientByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    @Override
    public void createClient(CreateClientDto createClientDto) {
        if(userService.existsByEmail(createClientDto.email())){
            throw new DuplicateEmail(createClientDto.email());
        }
        Client client = ClientMapper.INSTANCE.toEntity(createClientDto);

        clientRepository.save(client);
        userService.sendVerificationEmail(createClientDto.firstName(),createClientDto.email());
    }

    @Override
    public Client createClient(AccountClientIdDto request) {
        if (userService.existsByEmail(request.email())){
            throw new DuplicateEmail(request.email());
        }

        Client client = ClientMapper.INSTANCE.toEntity(request);
        if (request.privilege() != null) {
            client.setPrivileges(request.privilege());
        }
        Client savedClient = clientRepository.save(client);
        userService.sendVerificationEmail(request.firstName(), request.email());

        return savedClient;
    }

    @Override
    public void updateClient(UUID id, UpdateClientDto updateClientDto) {
        Client client = clientRepository.findById(id).orElseThrow(() -> new ClientNotFound(updateClientDto.email()));

        if (userService.existsByEmail(updateClientDto.email())) {
            throw new DuplicateEmail(updateClientDto.email());
        }

        ClientMapper.INSTANCE.fromUpdate(client, updateClientDto);
        if(updateClientDto.privilege() != null) {
            client.setPrivileges(updateClientDto.privilege());
        }

        clientRepository.save(client);
    }

    @Override
    public void activateClientAccount(Client client, String password) {
        client.setEnabled(true);
        client.setPassword(passwordEncoder.encode(password));
        clientRepository.save(client);
    }
}
