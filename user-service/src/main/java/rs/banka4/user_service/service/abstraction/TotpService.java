package rs.banka4.user_service.service.abstraction;

import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.authenticator.dtos.RegenerateAuthenticatorResponseDto;
import rs.banka4.user_service.exceptions.authenticator.NoTotpException;
import rs.banka4.user_service.exceptions.authenticator.NotActiveTotpException;
import rs.banka4.user_service.exceptions.authenticator.NotValidTotpException;
import rs.banka4.user_service.exceptions.user.NotAuthenticated;
import rs.banka4.user_service.exceptions.user.NotFound;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.repositories.UserTotpSecretRepository;
import rs.banka4.user_service.utils.JwtUtil;

/**
 * Service implementation for Time-based One-Time Password (TOTP) operations.
 * <p>
 * This service handles generation and validation of TOTP codes, secret management,
 * and authenticator configuration. It supports both Client and Employee entities.
 *
 * <h3>Key Configuration Parameters:</h3>
 * <ul>
 *   <li>Secret Length: 32 bytes (Base32 encoded)</li>
 *   <li>Time Period: 30 seconds</li>
 *   <li>Code Length: 6 digits</li>
 *   <li>Hashing Algorithm: SHA1</li>
 * </ul>
 *
 * <h3>Dependencies:</h3>
 * <ul>
 *   <li>{@link JwtUtil} - For JWT token handling and validation</li>
 *   <li>{@link UserTotpSecretRepository} - For TOTP secret persistence</li>
 *   <li>{@link ClientRepository} - For client entity lookups</li>
 *   <li>{@link EmployeeRepository} - For employee entity lookups</li>
 * </ul>
 *
 * @see HashingAlgorithm
 * @see DefaultSecretGenerator
 * @see DefaultCodeVerifier
 */
public interface TotpService {
    /**
     * Validates a TOTP code for an authenticated user.
     *
     * @param authorization Bearer token with user credentials
     * @param code 6-digit TOTP code to validate
     * @return true if code is valid and TOTP is active
     * @throws NotActiveTotpException if TOTP exists but isn't activated
     * @throws NoTotpException if no TOTP secret exists for the user
     * @throws NotAuthenticated if authorization token is invalid or expired
     */
    boolean validate(String authorization, String code);

    /**
     * Regenerates a new TOTP secret and returns provisioning details.
     *
     * @param auth Spring Security authentication object
     * @return DTO containing new secret and provisioning URL
     * @throws NotAuthenticated if token is expired or invalidated
     * @throws NotFound if no associated user entity exists
     */
    RegenerateAuthenticatorResponseDto regenerateSecret(Authentication auth);

    /**
     * Verifies and activates a newly generated TOTP secret.
     *
     * @param auth Spring Security authentication object
     * @param code 6-digit TOTP code generated with the new secret
     * @throws NotValidTotpException if code verification fails
     * @throws NoTotpException if no TOTP secret exists for the user
     * @throws NotAuthenticated if authentication credentials are invalid
     */
    void verifyNewAuthenticator(Authentication auth, String code);

    /**
     * Generates a current valid TOTP code for the authenticated user.
     *
     * @param authorization Bearer token with user credentials
     * @return 6-digit TOTP code
     * @throws NotAuthenticated if token is expired or invalidated
     * @throws NoTotpException if no TOTP secret exists for the user
     */
    String generateCode(String authorization);

    /**
     * Verifies a TOTP code for client authentication flow.
     *
     * @param authentication Spring Security authentication object
     * @param otpCode 6-digit TOTP code to validate
     * @return true if code is valid and TOTP is active
     * @throws NotAuthenticated if authentication credentials are invalid
     */
    boolean verifyClient(Authentication authentication, String otpCode);
}
