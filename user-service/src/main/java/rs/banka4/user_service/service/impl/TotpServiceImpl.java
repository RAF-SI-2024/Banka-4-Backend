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
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.authenticator.db.UserTotpSecret;
import rs.banka4.user_service.domain.authenticator.dtos.RegenerateAuthenticatorResponseDto;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.exceptions.authenticator.NoTotpException;
import rs.banka4.user_service.exceptions.authenticator.NotActiveTotpException;
import rs.banka4.user_service.exceptions.authenticator.NotValidTotpException;
import rs.banka4.user_service.exceptions.user.NotAuthenticated;
import rs.banka4.user_service.exceptions.user.NotFound;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.repositories.UserTotpSecretRepository;
import rs.banka4.user_service.service.abstraction.TotpService;
import rs.banka4.user_service.utils.JwtUtil;

@Service
@RequiredArgsConstructor
public class TotpServiceImpl implements TotpService {

    private static final int SECRET_LENGTH = 32;
    private static final int TIME_PERIOD = 30;
    private static final int CODE_LENGTH = 6;
    private static final HashingAlgorithm ALGORITHM = HashingAlgorithm.SHA1;

    private final JwtUtil jwtUtil;
    private final UserTotpSecretRepository repository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public void verifyNewAuthenticator(Authentication auth, String code) {
        String email = extractEmailFromAuth(auth);
        UserTotpSecret totp = getTotpSecretByEmail(email);
        validateTotpCode(totp, code);
        totp.setIsActive(true);
        repository.save(totp);
    }

    @Override
    public boolean validate(String authorization, String code) {
        String email = extractEmailFromToken(authorization);
        UserTotpSecret totp = getTotpSecretByEmail(email);
        if (!totp.getIsActive()) {
            throw new NotActiveTotpException();
        }
        return isCodeValid(totp.getSecret(), code);
    }

    @Override
    public RegenerateAuthenticatorResponseDto regenerateSecret(Authentication auth) {
        String token = extractToken(auth);
        String email = jwtUtil.extractUsername(token);

        if (jwtUtil.isTokenExpired(token)) throw new NotAuthenticated();
        if (jwtUtil.isTokenInvalidated(token)) throw new NotAuthenticated();

        SecretGenerator secretGenerator = new DefaultSecretGenerator(SECRET_LENGTH);
        String newSecret = secretGenerator.generate();

        UserTotpSecret userTotpSecret;
        Optional<Client> client = clientRepository.findByEmail(email);
        Optional<Employee> employee = employeeRepository.findByEmail(email);

        if (client.isPresent()) {
            Client safeClient = client.get();
            userTotpSecret =
                repository.findByClient_Email(email)
                    .map(secretObj -> {
                        secretObj.setSecret(newSecret);
                        secretObj.setIsActive(false);
                        return secretObj;
                    })
                    .orElseGet(() -> new UserTotpSecret(null, newSecret, safeClient, null, false));
        } else if (employee.isPresent()) {
            Employee safeEmployee = employee.get();
            userTotpSecret =
                repository.findByEmployee_Email(email)
                    .map(secretObj -> {
                        secretObj.setSecret(newSecret);
                        secretObj.setIsActive(false);
                        return secretObj;
                    })
                    .orElseGet(
                        () -> new UserTotpSecret(null, newSecret, null, safeEmployee, false)
                    );
        } else {
            throw new NotFound();
        }

        repository.save(userTotpSecret);

        return new RegenerateAuthenticatorResponseDto(
            createTotpUrl("RAFeisen", email, newSecret),
            newSecret
        );
    }

    @Override
    public boolean verifyClient(Authentication authentication, String otpCode) {
        return validate(authentication.getCredentials().toString(), otpCode);
    }

    @Override
    public String generateCode(String authorization) {
        String email = extractEmailFromToken(authorization);
        if (jwtUtil.isTokenExpired(authorization)) throw new NotAuthenticated();
        if (jwtUtil.isTokenInvalidated(authorization)) throw new NotAuthenticated();

        UserTotpSecret totp = getTotpSecretByEmail(email);
        return generateCodeFromSecret(totp.getSecret());
    }


    // --- Private helper methods ---

    private String generateCodeFromSecret(String secret) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(ALGORITHM);
        long currentBucket = timeProvider.getTime() / TIME_PERIOD;
        try {
            return codeGenerator.generate(secret, currentBucket);
        } catch (CodeGenerationException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isCodeValid(String secret, String code) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(ALGORITHM);
        DefaultCodeVerifier validator = new DefaultCodeVerifier(codeGenerator, timeProvider);
        return validator.isValidCode(secret, code);
    }

    private void validateTotpCode(UserTotpSecret totp, String code) {
        if (!isCodeValid(totp.getSecret(), code)) {
            throw new NotValidTotpException();
        }
    }

    private UserTotpSecret getTotpSecretByEmail(String email) {
        return repository.findByClient_Email(email)
            .or(() -> repository.findByEmployee_Email(email))
            .orElseThrow(NoTotpException::new);
    }

    private String extractEmailFromToken(String token) {
        String pureToken = token.replace("Bearer ", "");
        return jwtUtil.extractUsername(pureToken);
    }

    private String extractEmailFromAuth(Authentication auth) {
        return extractEmailFromToken(
            auth.getCredentials()
                .toString()
        );
    }

    private String extractToken(Authentication auth) {
        return auth.getCredentials()
            .toString()
            .replace("Bearer ", "");
    }

    private String createTotpUrl(String issuer, String email, String secret) {
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=%s&digits=%d&period=%d",
            issuer,
            email,
            secret,
            issuer,
            ALGORITHM,
            CODE_LENGTH,
            TIME_PERIOD
        );
    }
}
