package rs.banka4.user_service.service.impl;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.rafeisen.common.security.UserType;
import rs.banka4.user_service.domain.account.dtos.AccountClientIdDto;
import rs.banka4.user_service.domain.auth.dtos.LoginDto;
import rs.banka4.user_service.domain.auth.dtos.LoginResponseDto;
import rs.banka4.user_service.domain.authenticator.db.UserTotpSecret;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.client.dtos.*;
import rs.banka4.user_service.domain.user.client.mapper.ClientMapper;
import rs.banka4.user_service.exceptions.*;
import rs.banka4.user_service.exceptions.user.*;
import rs.banka4.user_service.exceptions.user.client.ClientNotFound;
import rs.banka4.user_service.exceptions.user.client.NonexistantSortByField;
import rs.banka4.user_service.exceptions.user.client.NotActivated;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.UserTotpSecretRepository;
import rs.banka4.user_service.security.PreAuthBankUserAuthentication;
import rs.banka4.user_service.security.UnauthenticatedBankUserPrincipal;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.service.abstraction.JwtService;
import rs.banka4.user_service.utils.specification.ClientSpecification;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final UserService userService;
    private final ClientRepository clientRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserTotpSecretRepository userTotpSecretRepository;
    private final JwtService jwtService;

    @Override
    public ResponseEntity<Page<ClientDto>> getClients(
        String firstName,
        String lastName,
        String email,
        String phone,
        String sortBy,
        PageRequest pageRequest
    ) {
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
        if (
            sortBy == null
                || sortBy.isEmpty()
                || "default".equalsIgnoreCase(sortBy)
                || "firstName".equalsIgnoreCase(sortBy)
        ) {
            sort = Sort.by("firstName");
        } else if ("lastName".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("lastName");
        } else if ("email".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("email");
        } else {
            throw new NonexistantSortByField(sortBy);
        }

        PageRequest pageRequestWithSort =
            PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), sort);

        Page<Client> clients = clientRepository.findAll(combinator.build(), pageRequestWithSort);
        Page<ClientDto> dtos = clients.map(ClientMapper.INSTANCE::toDto);
        return ResponseEntity.ok(dtos);
    }

    @Override
    public LoginResponseDto login(LoginDto loginDto) {
        final var principal =
            new UnauthenticatedBankUserPrincipal(UserType.CLIENT, loginDto.email());
        AuthenticatedBankUserAuthentication token;
        try {
            token =
                (AuthenticatedBankUserAuthentication) authenticationManager.authenticate(
                    new PreAuthBankUserAuthentication(principal, loginDto.password())
                );
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            throw new IncorrectCredentials();
        }

        Client client =
            clientRepository.findByEmail(loginDto.email())
                .orElseThrow(() -> new UsernameNotFoundException(loginDto.email()));
        if (client.getPassword() == null) {
            throw new NotActivated();
        }

        String accessToken = jwtService.generateAccessToken(client);
        String refreshToken = jwtService.generateRefreshToken(token.getPrincipal());

        return new LoginResponseDto(accessToken, refreshToken);
    }

    @Override
    public ClientDto getMe(String authorization) {
        String token = authorization.replace("Bearer ", "");
        UUID clientId = jwtService.extractUserId(token);

        if (jwtService.isTokenExpired(token)) throw new NotAuthenticated();

        Optional<UserTotpSecret> userTotpSecret =
            userTotpSecretRepository.findByClient_Id(clientId);
        boolean has2FA =
            userTotpSecret.map(UserTotpSecret::getIsActive)
                .orElse(false);

        Client client =
            clientRepository.findById(clientId)
                .orElseThrow(NotFound::new);

        return ClientMapper.INSTANCE.toDto(client, has2FA);
    }

    @Override
    public ClientDto getClientById(UUID id) {
        Client client =
            clientRepository.findById(id)
                .orElseThrow(NotFound::new);
        boolean has2FA =
            userTotpSecretRepository.findByClient_Id(id)
                .map(UserTotpSecret::getIsActive)
                .orElse(false);

        return ClientMapper.INSTANCE.toDto(client, has2FA);
    }

    @Override
    public Optional<Client> getClientByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    @Override
    public Optional<Client> findClientById(UUID id) {
        return clientRepository.findById(id);
    }

    @Override
    public void createClient(CreateClientDto createClientDto) {
        if (userService.existsByEmail(createClientDto.email())) {
            throw new DuplicateEmail(createClientDto.email());
        }
        if (!userService.isPhoneNumberValid(createClientDto.phone())) {
            throw new InvalidPhoneNumber();
        }
        Client client = ClientMapper.INSTANCE.toEntity(createClientDto);

        clientRepository.save(client);
        userService.sendVerificationEmail(createClientDto.firstName(), createClientDto.email());
    }

    @Override
    public Client createClient(AccountClientIdDto request) {
        if (userService.existsByEmail(request.email())) {
            throw new DuplicateEmail(request.email());
        }

        if (!userService.isPhoneNumberValid(request.phone())) {
            throw new InvalidPhoneNumber();
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
        Client client =
            clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFound(updateClientDto.email()));

        boolean hasTradePrivilege =
            client.getPrivileges()
                .contains(Privilege.TRADE);

        if (userService.existsByEmail(updateClientDto.email())) {
            throw new DuplicateEmail(updateClientDto.email());
        }

        if (!userService.isPhoneNumberValid(updateClientDto.phoneNumber())) {
            throw new InvalidPhoneNumber();
        }

        ClientMapper.INSTANCE.fromUpdate(client, updateClientDto);
        if (updateClientDto.privilege() != null) {
            client.setPrivileges(updateClientDto.privilege());
        }

        if (
            hasTradePrivilege
                && !client.getPrivileges()
                    .contains(Privilege.TRADE)
        ) {
            client.getPrivileges()
                .add(Privilege.TRADE);
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
