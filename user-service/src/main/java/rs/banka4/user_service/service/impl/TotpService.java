package rs.banka4.user_service.service.impl;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.ClientDto;
import rs.banka4.user_service.dto.RegenerateAuthenticatorResponseDto;
import rs.banka4.user_service.exceptions.*;
import rs.banka4.user_service.models.Client;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.models.UserTotpSecret;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.repositories.UserTotpSecretRepository;
import rs.banka4.user_service.service.abstraction.TotpAbs;
import rs.banka4.user_service.utils.JwtUtil;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TotpService implements TotpAbs {

    private static final int SECRET_LENGTH = 32;
    private static final int TIME_PERIOD = 30;
    private static final int CODE_LENGTH = 6;
    private static final HashingAlgorithm ALGORITHM = HashingAlgorithm.SHA1;

    private final JwtUtil jwtUtil;
    private final UserTotpSecretRepository repository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;


    @Override
    public ResponseEntity<Void> verifyNewAuthenticator(Authentication auth, String code) {

        String token = auth.getCredentials().toString().replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        UserTotpSecret totp = null;
        if(repository.findByClient_Email(email).isPresent()){
            totp = repository.findByClient_Email(email).get();
        } else {
            totp = repository.findByEmployee_Email(email).get();
        }
        
        if(totp == null){
            throw new NoTotpException();
        }

        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(ALGORITHM);
        DefaultCodeVerifier validator = new DefaultCodeVerifier(codeGenerator, timeProvider);
        String secret = totp.getSecret();
        if(validator.isValidCode(secret,code)){
            totp.setIsActive(true);
            repository.save(totp);
        }else{
            throw new NotValidTotpException();
        }


        return ResponseEntity.ok().build();
    }

    @Override
    public boolean validate(String authorization, String code){
        String token = authorization.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        UserTotpSecret totp = null;
        if(repository.findByClient_Email(email).isPresent()){
            totp = repository.findByClient_Email(email).get();
        } else {
            totp = repository.findByEmployee_Email(email).get();
        }
        if(totp == null){
            throw new NoTotpException();
        }
        if(!totp.getIsActive()){
            throw new NotActiveTotpException();
        }

        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(ALGORITHM);
        DefaultCodeVerifier validator = new DefaultCodeVerifier(codeGenerator, timeProvider);
        String secret = totp.getSecret();
        return validator.isValidCode(secret, code);
    }

    @Override
    public ResponseEntity<RegenerateAuthenticatorResponseDto> regenerateSecret(Authentication auth) {
        String token = auth.getCredentials().toString().replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        if (jwtUtil.isTokenExpired(token)) throw new NotAuthenticated();
        if (jwtUtil.isTokenInvalidated(token)) throw new NotAuthenticated();

        SecretGenerator secretGenerator = new DefaultSecretGenerator(SECRET_LENGTH);
        String newSecret = secretGenerator.generate();

        UserTotpSecret userTotpSecret;
        System.out.println("user regen " + email + " " + token);
        System.out.println("user regen " + SecurityContextHolder.getContext().getAuthentication());
        Optional<Client> client = clientRepository.findByEmail(email);
        Optional<Employee> employee = employeeRepository.findByEmail(email);

        if (client.isPresent()) {
            Client safeClient = client.get();
            clientRepository.findByEmail(email)
                    .orElseThrow(NotFound::new);
            userTotpSecret = repository.findByClient_Email(email)
                    .map(userTotpSecret1 -> {
                        userTotpSecret1.setSecret(newSecret);
                        userTotpSecret1.setIsActive(false);
                        return userTotpSecret1;
                    })
                    .orElseGet(() -> new UserTotpSecret(null,
                            newSecret,
                            safeClient,
                            null,
                            false));

        } else if (employee.isPresent()) {
            Employee safeEmployee = employeeRepository.findByEmail(email)
                    .orElseThrow(NotFound::new);

            userTotpSecret = repository.findByEmployee_Email(email)
                    .map(userTotpSecret1 -> {
                        userTotpSecret1.setSecret(newSecret);
                        userTotpSecret1.setIsActive(false);
                        return userTotpSecret1;
                    })
                    .orElseGet(() -> new UserTotpSecret(null,
                            newSecret,
                            null,
                            safeEmployee,
                            false));

        } else {
            System.out.println("user regen not fount");
            throw new NotFound();
        }

        repository.save(userTotpSecret);

        return ResponseEntity.ok(
                new RegenerateAuthenticatorResponseDto(
                        createTotpUrl("RAFeisen", email, newSecret),
                        newSecret));
    }

    /*
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String secret;

    @OneToOne
    @JoinColumn(name = "client_id", unique = true)
    private Client client;

    @OneToOne
    @JoinColumn(name = "employee_id", unique = true)
    private Employee employee;

    @Column(nullable = false)
    private Boolean isActive;
     */


    @Override
    public String generateCode(String authorization){
        String token = authorization.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        if(jwtUtil.isTokenExpired(token)) throw new NotAuthenticated();
        if(jwtUtil.isTokenInvalidated(token)) throw new NotAuthenticated();

        UserTotpSecret totp = null;
        if(repository.findByClient_Email(email).isPresent()){
            totp = repository.findByClient_Email(email).get();
        }else{
            totp = repository.findByEmployee_Email(email).get();
        }
        if(totp == null){
            throw new NoTotpException();
        }



        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(ALGORITHM);
        String secret = totp.getSecret();
        long currentBucket = timeProvider.getTime() / TIME_PERIOD;
        try {
            return codeGenerator.generate(secret, currentBucket);
        } catch (CodeGenerationException e) {
            throw new RuntimeException(e);
        }
    }


    private String createTotpUrl(String issuer,
                                 String email,
                                 String secret) {
        String totpUri = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=%s&digits=%d&period=%d",
                issuer, email, secret, issuer, ALGORITHM, CODE_LENGTH, TIME_PERIOD
        );
        return totpUri;
    }

}
