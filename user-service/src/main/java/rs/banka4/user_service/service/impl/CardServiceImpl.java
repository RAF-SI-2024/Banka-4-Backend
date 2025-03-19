package rs.banka4.user_service.service.impl;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.exceptions.NullPageRequest;
import rs.banka4.user_service.exceptions.account.AccountNotFound;
import rs.banka4.user_service.exceptions.account.NotAccountOwner;
import rs.banka4.user_service.exceptions.authenticator.NotValidTotpException;
import rs.banka4.user_service.exceptions.card.AuthorizedUserNotAllowed;
import rs.banka4.user_service.exceptions.card.CardLimitExceededException;
import rs.banka4.user_service.exceptions.card.DuplicateAuthorizationException;
import rs.banka4.user_service.exceptions.card.NotValidCardStatus;
import rs.banka4.user_service.exceptions.user.NotAuthenticated;
import rs.banka4.user_service.exceptions.user.client.ClientNotFound;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.CardRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.abstraction.CardService;
import rs.banka4.user_service.service.abstraction.TotpService;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.specification.CardSpecification;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

@Service
@Primary
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final TotpService totpService;
    private final JwtUtil jwtUtil;
    private final ClientRepository clientRepository;

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
        String token,
        String accountNumber,
        Pageable pageable
    ) {

        String email = jwtUtil.extractUsername(token);

        Optional<Client> client = clientRepository.findByEmail(email);

        if (client.isEmpty()) throw new ClientNotFound(email);

        Set<Account> accounts = accountRepository.findAllByClient(client.get());

        boolean found =
            accounts.stream()
                .map(Account::getAccountNumber)
                .collect(Collectors.toSet())
                .contains(accountNumber);

        if (!found) throw new NotAccountOwner();

        List<Card> clientCards = cardRepository.findByAccountAccountNumber(accountNumber);

        List<CardDto> cardDtos =
            clientCards.stream()
                .map(CardMapper.INSTANCE::toDto)
                .toList();

        Page<CardDto> pagedClientCards = new PageImpl<>(cardDtos, pageable, clientCards.size());

        return ResponseEntity.ok(pagedClientCards);
    }

    // Private functions
    @Override
    public ResponseEntity<Page<CardDto>> employeeSearchCards(
        String token,
        String cardNumber,
        String firstName,
        String lastName,
        String email,
        String cardStatus,
        Pageable pageable
    ) {

        if (
            !jwtUtil.extractRole(token)
                .equals("employee")
        ) throw new NotAuthenticated();

        if (pageable == null) {
            throw new NullPageRequest();
        }

        SpecificationCombinator<Card> combinator = new SpecificationCombinator<>();

        if (cardNumber != null && !cardNumber.isEmpty()) {
            combinator.and(CardSpecification.hasCardNumber(cardNumber));
        }

        if (firstName != null && !firstName.isEmpty()) {
            combinator.and(CardSpecification.hasFirstName(firstName));
        }

        if (lastName != null && !lastName.isEmpty()) {
            combinator.and(CardSpecification.hasLastName(lastName));
        }

        if (email != null && !email.isEmpty()) {
            combinator.and(CardSpecification.hasEmail(email));
        }

        if (cardStatus != null && !cardStatus.isEmpty()) {
            try {
                CardStatus statusEnum = CardStatus.valueOf(cardStatus.toUpperCase());
                combinator.and(CardSpecification.hasCardStatus(statusEnum.name()));
            } catch (IllegalArgumentException e) {
                throw new NotValidCardStatus();
            }
        }

        Page<Card> cards = cardRepository.findAll(combinator.build(), pageable);
        Page<CardDto> dtos = cards.map(CardMapper.INSTANCE::toDtoWithDetails);

        return ResponseEntity.ok(dtos);
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
