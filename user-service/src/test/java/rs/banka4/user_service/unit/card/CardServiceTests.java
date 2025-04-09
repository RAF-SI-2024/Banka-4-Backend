package rs.banka4.user_service.unit.card;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import rs.banka4.rafeisen.common.dto.Gender;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.account.db.AccountType;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.db.CardStatus;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedUserDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;
import rs.banka4.user_service.exceptions.card.AuthorizedUserNotAllowed;
import rs.banka4.user_service.exceptions.card.CardLimitExceededException;
import rs.banka4.user_service.exceptions.card.DuplicateAuthorizationException;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.CardRepository;
import rs.banka4.user_service.service.abstraction.JwtService;
import rs.banka4.user_service.service.impl.CardServiceImpl;
import rs.banka4.user_service.service.impl.TotpServiceImpl;
import rs.banka4.user_service.service.impl.UserService;

@ExtendWith(MockitoExtension.class)
public class CardServiceTests {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private JwtService jwtService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TotpServiceImpl totpService;

    @InjectMocks
    private CardServiceImpl cardService;

    @Mock
    private UserService userService;

    // Sample DTOs for testing different account types
    private CreateCardDto validBusinessRequestWithAuthorizedUser;
    private CreateCardDto validBusinessRequestWithoutAuthorizedUser;
    private CreateCardDto validPersonalRequest;

    private Account businessAccount;
    private Account personalAccount;

    private Authentication authentication;
    private Authentication authentication2;
    private Card card;
    private final String cardNumber = "1234567890123456";
    private final String token = "mockToken";

    @BeforeEach
    public void setUp() {
        // Create a mock Authentication with non-null credentials
        authentication = mock(Authentication.class);
        lenient().when(authentication.getCredentials())
            .thenReturn("dummyTotp");

        // Initialize a business account (e.g., type DOO which is a business account)
        businessAccount = new Account();
        businessAccount.setAccountNumber("BUSINESS123");
        businessAccount.setAccountType(AccountType.DOO);

        // Initialize a personal account (e.g., type STANDARD which is a personal account)
        personalAccount = new Account();
        personalAccount.setAccountNumber("PERSONAL123");
        personalAccount.setAccountType(AccountType.STANDARD);

        // Create valid DTO for a business account with an authorized user
        validBusinessRequestWithAuthorizedUser =
            new CreateCardDto(
                businessAccount.getAccountNumber(),
                new CreateAuthorizedUserDto(
                    "John",
                    "Doe",
                    LocalDate.of(1980, 1, 1),
                    Gender.MALE,
                    "john.doe@example.com",
                    "123456789",
                    "123 Business St"
                ),
                "dummyTotp"
            );
        // Create valid DTO for a business account without an authorized user
        validBusinessRequestWithoutAuthorizedUser =
            new CreateCardDto(businessAccount.getAccountNumber(), null, "dummyTotp");
        // Create valid DTO for a personal account (authorizedUser must be null)
        validPersonalRequest =
            new CreateCardDto(personalAccount.getAccountNumber(), null, "dummyTotp");

        authentication2 = mock(Authentication.class);
        lenient().when(authentication2.getCredentials())
            .thenReturn(token);

        card = new Card();
        card.setCardNumber(cardNumber);
        card.setCardStatus(CardStatus.ACTIVATED);
    }

    @Test
    void testBlockCard_AlreadyBlocked() {
        card.setCardStatus(CardStatus.BLOCKED);
        when(cardRepository.findCardByCardNumber(card.getCardNumber())).thenReturn(
            Optional.of(card)
        );

        Card blockedCard = cardService.blockCard(card.getCardNumber(), token);
        assertNull(blockedCard);
    }

    @Test
    void testBlockCard_CardNotFound() {
        when(cardRepository.findCardByCardNumber(card.getCardNumber())).thenReturn(
            Optional.empty()
        );

        Card blockedCard = cardService.blockCard(card.getCardNumber(), token);
        assertNull(blockedCard);
    }

    @Test
    void testBlockCard_InvalidUser() {
        when(cardRepository.findCardByCardNumber(card.getCardNumber())).thenReturn(
            Optional.of(card)
        );
        when(jwtService.extractRole(token)).thenReturn("client");
        when(jwtService.extractUserId(token)).thenReturn(
            UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        );

        Card blockedCard = cardService.blockCard(card.getCardNumber(), token);
        assertNull(blockedCard);
    }

    @Test
    void testUnblockCard_CardNotFound() {
        when(cardRepository.findCardByCardNumber(card.getCardNumber())).thenReturn(
            Optional.empty()
        );

        Card unblockedCard = cardService.unblockCard(card.getCardNumber(), token);
        assertNull(unblockedCard);
    }

    @Test
    void testUnblockCard_InvalidUser() {
        when(cardRepository.findCardByCardNumber(card.getCardNumber())).thenReturn(
            Optional.of(card)
        );
        when(jwtService.extractRole(token)).thenReturn("client");

        Card unblockedCard = cardService.unblockCard(card.getCardNumber(), token);
        assertNull(unblockedCard);
    }

    @Test
    void testUnblockCard_AlreadyActive() {
        card.setCardStatus(CardStatus.ACTIVATED);
        when(cardRepository.findCardByCardNumber(card.getCardNumber())).thenReturn(
            Optional.of(card)
        );

        Card unblockedCard = cardService.unblockCard(card.getCardNumber(), token);
        assertNull(unblockedCard);
    }

    @Test
    void testDeactivateCard_CardNotFound() {
        when(cardRepository.findCardByCardNumber(card.getCardNumber())).thenReturn(
            Optional.empty()
        );

        Card deactivatedCard = cardService.deactivateCard(card.getCardNumber(), token);
        assertNull(deactivatedCard);
    }

    @Test
    void testDeactivateCard_InvalidUser() {
        when(cardRepository.findCardByCardNumber(card.getCardNumber())).thenReturn(
            Optional.of(card)
        );
        when(jwtService.extractRole(token)).thenReturn("client");

        Card deactivatedCard = cardService.deactivateCard(card.getCardNumber(), token);
        assertNull(deactivatedCard);
    }

    @Test
    void testDeactivateCard_AlreadyDeactivated() {
        card.setCardStatus(CardStatus.DEACTIVATED);
        when(cardRepository.findCardByCardNumber(card.getCardNumber())).thenReturn(
            Optional.of(card)
        );

        Card deactivatedCard = cardService.deactivateCard(card.getCardNumber(), token);
        assertNull(deactivatedCard);
    }

    @Test
    public void testCreateAuthorizedCard_Success_BusinessAccount_WithAuthorizedUser() {
        // Business account with an authorized user; duplicate check must return false.
        when(totpService.validate("dummyTotp", validBusinessRequestWithAuthorizedUser.otpCode()))
            .thenReturn(true);
        when(accountRepository.findAccountByAccountNumber(businessAccount.getAccountNumber()))
            .thenReturn(Optional.of(businessAccount));
        when(cardRepository.countByAccount(businessAccount)).thenReturn(0);
        when(
            cardRepository.existsByAccountAndAuthorizedUserEmail(
                businessAccount,
                "john.doe@example.com"
            )
        ).thenReturn(false);
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);

        when(userService.isPhoneNumberValid(any())).thenReturn(true);

        cardService.createAuthorizedCard(authentication, validBusinessRequestWithAuthorizedUser);

        verify(cardRepository).save(any(Card.class));
    }

    @Test
    public void testCreateAuthorizedCard_Success_BusinessAccount_WithoutAuthorizedUser() {
        // Business account without authorized user; allowed if no card exists.
        when(totpService.validate("dummyTotp", validBusinessRequestWithoutAuthorizedUser.otpCode()))
            .thenReturn(true);
        when(accountRepository.findAccountByAccountNumber(businessAccount.getAccountNumber()))
            .thenReturn(Optional.of(businessAccount));
        // For business accounts without an authorized user, max allowed cards is 1.
        when(cardRepository.countByAccount(businessAccount)).thenReturn(0);
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);

        cardService.createAuthorizedCard(authentication, validBusinessRequestWithoutAuthorizedUser);

        verify(cardRepository).save(any(Card.class));
    }

    @Test
    public void testCreateAuthorizedCard_Failure_BusinessAccount_WithDuplicateAuthorizedUser() {
        // For business account with authorized user, if duplicate exists, throw exception.
        when(totpService.validate("dummyTotp", validBusinessRequestWithAuthorizedUser.otpCode()))
            .thenReturn(true);
        when(accountRepository.findAccountByAccountNumber(businessAccount.getAccountNumber()))
            .thenReturn(Optional.of(businessAccount));
        when(cardRepository.countByAccount(businessAccount)).thenReturn(0);
        // Simulate duplicate authorized user exists
        when(
            cardRepository.existsByAccountAndAuthorizedUserEmail(
                businessAccount,
                "john.doe@example.com"
            )
        ).thenReturn(true);

        when(userService.isPhoneNumberValid(any())).thenReturn(true);

        assertThrows(DuplicateAuthorizationException.class, () -> {
            cardService.createAuthorizedCard(
                authentication,
                validBusinessRequestWithAuthorizedUser
            );
        });
    }

    @Test
    public void testCreateAuthorizedCard_Success_PersonalAccount() {
        // For personal account, authorizedUser must be null and card count must be less than 2.
        when(totpService.validate("dummyTotp", validPersonalRequest.otpCode())).thenReturn(true);
        when(accountRepository.findAccountByAccountNumber(personalAccount.getAccountNumber()))
            .thenReturn(Optional.of(personalAccount));
        // Personal account: currently 1 card exists, so a new card is allowed.
        when(cardRepository.countByAccount(personalAccount)).thenReturn(1);
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);

        cardService.createAuthorizedCard(authentication, validPersonalRequest);

        verify(cardRepository).save(any(Card.class));
    }

    @Test
    public void testCreateAuthorizedCard_Failure_PersonalAccount_WithAuthorizedUser() {
        // For personal account, providing an authorized user is not allowed.
        CreateCardDto requestWithAuthorizedUser =
            new CreateCardDto(
                personalAccount.getAccountNumber(),
                new CreateAuthorizedUserDto(
                    "Jane",
                    "Doe",
                    LocalDate.of(1990, 1, 1),
                    Gender.FEMALE,
                    "jane.doe@example.com",
                    "+381664203512",
                    "123 Personal St"
                ),
                "dummyTotp"
            );
        when(totpService.validate("dummyTotp", requestWithAuthorizedUser.otpCode())).thenReturn(
            true
        );
        when(userService.isPhoneNumberValid(any())).thenReturn(true);
        when(accountRepository.findAccountByAccountNumber(personalAccount.getAccountNumber()))
            .thenReturn(Optional.of(personalAccount));
        assertThrows(AuthorizedUserNotAllowed.class, () -> {
            cardService.createAuthorizedCard(authentication, requestWithAuthorizedUser);
        });
    }

    @Test
    public void testCreateAuthorizedCard_Failure_PersonalAccount_ExceedingLimit() {
        // For personal account, if the card count is already 2 or more, throw exception.
        when(totpService.validate("dummyTotp", validPersonalRequest.otpCode())).thenReturn(true);
        when(accountRepository.findAccountByAccountNumber(personalAccount.getAccountNumber()))
            .thenReturn(Optional.of(personalAccount));
        when(cardRepository.countByAccount(personalAccount)).thenReturn(2);

        assertThrows(CardLimitExceededException.class, () -> {
            cardService.createAuthorizedCard(authentication, validPersonalRequest);
        });
    }
}
