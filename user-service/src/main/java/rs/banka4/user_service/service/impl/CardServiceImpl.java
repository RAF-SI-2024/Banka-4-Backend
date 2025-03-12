package rs.banka4.user_service.service.impl;

import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.db.CardStatus;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedUserDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;
import rs.banka4.user_service.domain.card.mapper.CardMapper;
import rs.banka4.user_service.exceptions.account.AccountNotFound;
import rs.banka4.user_service.exceptions.authenticator.NotValidTotpException;
import rs.banka4.user_service.exceptions.card.AuthorizedUserNotAllowed;
import rs.banka4.user_service.exceptions.card.CardLimitExceededException;
import rs.banka4.user_service.exceptions.card.DuplicateAuthorizationException;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.CardRepository;
import rs.banka4.user_service.service.abstraction.CardService;
import rs.banka4.user_service.utils.JwtUtil;

@Service
@Primary
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final TotpService totpService;
    private final JwtUtil jwtUtil;


    @Transactional
    public void createAuthorizedCard(Authentication auth, CreateCardDto dto) {
        if (
            !totpService.validate(
                auth.getCredentials()
                    .toString(),
                dto.otpCode()
            )
        ) {
            throw new NotValidTotpException();
        }

        Account account =
            accountRepository.findAccountByAccountNumber(dto.accountNumber())
                .orElseThrow(AccountNotFound::new);

        validateCardLimits(account, dto.authorizedUser());

        Card card = CardMapper.INSTANCE.fromCreate(dto);
        card.setCardNumber(generateUniqueCardNumber());
        card.setCvv(generateRandomCVV());
        card.setAccount(account);
        card.setCardName(CardMapper.INSTANCE.mapCardName(account));

        cardRepository.save(card);
    }

    @Override
    public Card blockCard(String cardNumber, String token) {
        Optional<Card> optionalCard = cardRepository.findCardByCardNumber(cardNumber);
        if (optionalCard.isEmpty()) {
            return null;
        }

        Card card = optionalCard.get();
        String role = jwtUtil.extractRole(token);
        String userId = jwtUtil.extractClaim(token, claims -> claims.get("id", String.class));
        String email = jwtUtil.extractUsername(token);

        if ("client".equalsIgnoreCase(role)) {
            if (
                card.getAccount() == null
                    || card.getAccount()
                        .getClient()
                        == null
            ) {
                return null;
            }
            String ownerId =
                card.getAccount()
                    .getClient()
                    .getId()
                    .toString();
            String ownerEmail =
                card.getAccount()
                    .getClient()
                    .getEmail();

            if (!userId.equals(ownerId) && !email.equals(ownerEmail)) {
                return null;
            }
        }
        if (
            card.getCardStatus() == CardStatus.BLOCKED
                || card.getCardStatus() == CardStatus.DEACTIVATED
        ) {
            return card;
        }
        card.setCardStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    @Override
    public Card unblockCard(String cardNumber, String token) {
        Optional<Card> optionalCard = cardRepository.findCardByCardNumber(cardNumber);
        if (optionalCard.isEmpty()) {
            return null;
        }

        Card card = optionalCard.get();
        String role = jwtUtil.extractRole(token);

        if (!"employee".equalsIgnoreCase(role)) {
            return null;
        }

        if (card.getCardStatus() != CardStatus.BLOCKED) {
            return card;
        }

        card.setCardStatus(CardStatus.ACTIVATED);
        return cardRepository.save(card);
    }


    @Override
    public Card deactivateCard(String cardNumber, String token) {
        Optional<Card> optionalCard = cardRepository.findCardByCardNumber(cardNumber);
        if (optionalCard.isEmpty()) {
            return null;
        }

        Card card = optionalCard.get();
        String role = jwtUtil.extractRole(token);

        if (!"employee".equalsIgnoreCase(role)) {
            return null;
        }

        if (card.getCardStatus() == CardStatus.DEACTIVATED) {
            return null;
        }

        card.setCardStatus(CardStatus.DEACTIVATED);
        return cardRepository.save(card);
    }


    @Override
    public ResponseEntity<Page<CardDto>> clientSearchCards(
        String accountNumber,
        Pageable pageable
    ) {
        return null;
        // check out /client/search
    }

    // Private functions
    @Override
    public ResponseEntity<Page<CardDto>> employeeSearchCards(
        String cardNumber,
        String firstName,
        String lastName,
        String email,
        String cardStatus,
        Pageable pageable
    ) {
        return null;
        // check out /client/search
    }

    // ---- Private methods ----

    @Transactional
    public void createEmployeeCard(CreateCardDto dto, Account account) {
        validateCardLimits(account, null);

        Card card = CardMapper.INSTANCE.fromCreate(dto);
        card.setCardNumber(generateUniqueCardNumber());
        card.setCvv(generateRandomCVV());
        card.setAccount(account);
        card.setCardName(CardMapper.INSTANCE.mapCardName(account));

        cardRepository.save(card);
    }

    private void validateCardLimits(
        Account account,
        @Nullable CreateAuthorizedUserDto authorizedUser
    ) {
        if (
            account.getAccountType()
                .isBusiness()
        ) {
            int existingCards = cardRepository.countByAccount(account);

            if (authorizedUser == null) {
                if (existingCards >= 1) {
                    throw new AuthorizedUserNotAllowed();
                }
            } else {
                if (
                    cardRepository.existsByAccountAndAuthorizedUserEmail(
                        account,
                        authorizedUser.email()
                    )
                ) {
                    throw new DuplicateAuthorizationException();
                }
            }
        } else {
            if (authorizedUser != null) {
                throw new AuthorizedUserNotAllowed();
            }
            int existingCards = cardRepository.countByAccount(account);
            if (existingCards >= 2) {
                throw new CardLimitExceededException();
            }
        }
    }

    private String generateUniqueCardNumber() {
        String number;
        do {
            number =
                String.format(
                    "%016d",
                    ThreadLocalRandom.current()
                        .nextLong(1_000_000_000_000_000L)
                );
        } while (cardRepository.existsByCardNumber(number));
        return number;
    }

    private String generateRandomCVV() {
        return String.format(
            "%03d",
            ThreadLocalRandom.current()
                .nextInt(1000)
        );
    }

}
