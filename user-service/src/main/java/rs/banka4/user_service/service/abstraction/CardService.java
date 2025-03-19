package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;
import rs.banka4.user_service.exceptions.account.AccountNotFound;
import rs.banka4.user_service.exceptions.authenticator.NotValidTotpException;
import rs.banka4.user_service.exceptions.card.AuthorizedUserNotAllowed;
import rs.banka4.user_service.exceptions.card.CardLimitExceededException;
import rs.banka4.user_service.exceptions.card.DuplicateAuthorizationException;
import rs.banka4.user_service.repositories.CardRepository;
import rs.banka4.user_service.utils.JwtUtil;

/**
 * Service interface for card management operations.
 * <p>
 * Handles card lifecycle operations including creation, status changes, and search functionality.
 * Supports both client-facing and employee-specific operations with proper authorization checks.
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Card creation with TOTP verification</li>
 * <li>Card status management (block/unblock/deactivate)</li>
 * <li>Role-based access control</li>
 * <li>Pagination support for search operations</li>
 * </ul>
 *
 * <ul>
 * <li>Card number/CVV generation</li>
 * <li>Account-card relationship management</li>
 * <li>Role-based authorization checks</li>
 * <li>Business/personal account differentiation</li>
 * </ul>
 *
 * <h3>Security Constraints:</h3>
 * <ul>
 * <li>Clients can only block their own cards</li>
 * <li>Card unblocking/deactivation requires employee role</li>
 * <li>Business accounts have different card limits than personal accounts</li>
 * </ul>
 *
 * @see CardRepository
 * @see TotpService
 * @see JwtUtil
 */
public interface CardService {
    /**
     * Creates a new card with TOTP verification and business rule validation.
     *
     * @param auth Spring Security authentication context
     * @param createCardDto Card creation parameters
     * @throws NotValidTotpException if OTP code validation fails
     * @throws AccountNotFound if referenced account doesn't exist
     * @throws AuthorizedUserNotAllowed if unauthorized user configuration
     * @throws CardLimitExceededException if account card limit reached
     *         <p>
     *         Generation details:
     *         <ul>
     *         <li>16-digit unique card number</li>
     *         <li>3-digit random CVV</li>
     *         <li>Automatic card naming based on account</li>
     *         </ul>
     *
     * @throws AuthorizedUserNotAllowed for personal accounts with authorized users
     * @throws DuplicateAuthorizationException for duplicate business authorized users
     */
    void createAuthorizedCard(Authentication auth, CreateCardDto createCardDto);

    /**
     * Blocks a card based on authorization rules.
     *
     * @param cardNumber 16-digit card number
     * @param token JWT authentication token
     * @return Updated card entity or null if unauthorized
     * @throws SecurityException if client attempts to block another user's card
     *         <ul>
     *         <li>Clients can only block their own cards</li>
     *         <li>Employees can block any card</li>
     *         <li>No-op if card already blocked/deactivated</li>
     *         </ul>
     */
    Card blockCard(String cardNumber, String token);

    /**
     * Unblocks a card (employee-only operation).
     *
     * @param cardNumber 16-digit card number
     * @param token JWT authentication token
     * @return Updated card entity or null if unauthorized
     * @throws SecurityException if attempted by non-employee
     *         <p>
     *         Returns original card if:
     *         <ul>
     *         <li>Card not in blocked state</li>
     *         <li>Caller lacks employee privileges</li>
     *         </ul>
     *
     */
    Card unblockCard(String cardNumber, String token);

    /**
     * Permanently deactivates a card (employee-only operation).
     *
     * @param cardNumber 16-digit card number
     * @param token JWT authentication token
     * @return Updated card entity or null if unauthorized
     * @throws SecurityException if attempted by non-employee
     */
    Card deactivateCard(String cardNumber, String token);

    /**
     * Client-focused card search by account number.
     *
     * @param accountNumber Account identifier
     * @param pageable Pagination parameters
     * @return Paginated card DTOs wrapped in ResponseEntity
     */
    ResponseEntity<Page<CardDto>> clientSearchCards(
        String token,
        String accountNumber,
        Pageable pageable
    );

    /**
     * Employee card search with multiple filters.
     *
     * @param cardNumber Partial/full card number
     * @param firstName Cardholder first name
     * @param lastName Cardholder last name
     * @param email Cardholder email
     * @param cardStatus Card status filter
     * @param pageable Pagination parameters
     * @return Paginated card DTOs wrapped in ResponseEntity
     */
    ResponseEntity<Page<CardDto>> employeeSearchCards(
        String token,
        String cardNumber,
        String firstName,
        String lastName,
        String email,
        String cardStatus,
        Pageable pageable
    );

    /**
     * Internal method for employee card creation (no TOTP required).
     *
     * @param dto Card creation parameters
     * @param account Associated account entity
     */
    void createEmployeeCard(CreateCardDto dto, Account account);
}
