package rs.banka4.user_service.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.account.db.AccountType;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.dtos.CardDto;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedUserDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;
import rs.banka4.user_service.domain.card.mapper.CardMapper;
import rs.banka4.user_service.exceptions.account.AccountNotFound;
import rs.banka4.user_service.exceptions.authenticator.NotValidTotpException;
import rs.banka4.user_service.exceptions.card.CardLimitExceededException;
import rs.banka4.user_service.exceptions.card.DuplicateAuthorizationException;
import rs.banka4.user_service.exceptions.user.NotAuthenticated;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.CardRepository;
import rs.banka4.user_service.service.abstraction.CardService;

import javax.annotation.Nullable;
import javax.security.auth.login.AccountNotFoundException;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final TotpService totpService;
    private final CardMapper cardMapper;


    @Transactional
    public Card createAuthorizedCard(Authentication auth,
                                     CreateCardDto dto) {
        if (!totpService.validate(auth.getCredentials().toString(), dto.otpCode())) {
            throw new NotValidTotpException();
        }

        Account account = accountRepository.findAccountByAccountNumber(dto.accountNumber())
                .orElseThrow(AccountNotFound::new);

        validateCardLimits(account, dto.createAuthorizedUserDto());

        Card card = cardMapper.fromCreate(dto);
        card.setCardNumber(generateUniqueCardNumber());
        card.setCvv(generateRandomCVV());
        card.setAccount(account);

        return cardRepository.save(card);
    }

    @Override
    public Card blockCard(String cardNumber) {
        return null;
    }

    @Override
    public Card unblockCard(String cardNumber) {
        return null;
    }

    @Override
    public Card deactivateCard(String cardNumber) {
        return null;
    }

    @Override
    public ResponseEntity<Page<CardDto>> clientSearchCards(String accountNumber, Pageable pageable) {
        return null;
        // check out /client/search
    }

    // Private functions
    @Override
    public ResponseEntity<Page<CardDto>> employeeSearchCards(String cardNumber, String firstName, String lastName, String email, String cardStatus, Pageable pageable) {
        return null;
        // check out /client/search
    }

    // ---- Private methods ----

    private void validateCardLimits(Account account,
                                    @Nullable CreateAuthorizedUserDto authorizedUser) {
        int existingCards = cardRepository.countByAccount(account);

        if (account.getAccountType() == AccountType.STANDARD) {
            if (existingCards >= 2) throw new CardLimitExceededException();
        } else {
            if (authorizedUser == null) throw new NotAuthenticated();
            if (cardRepository.existsByAccountAndAuthorizedUserEmail(account, authorizedUser.email())) {
                throw new DuplicateAuthorizationException();
            }
            if (existingCards >= 1) throw new CardLimitExceededException();
        }
    }

    private String generateUniqueCardNumber() {
        String number;
        do {
            number = String.format("%016d", ThreadLocalRandom.current().nextLong(1_000_000_000_000_000L));
        } while (cardRepository.existsByCardNumber(number));
        return number;
    }

    private String generateRandomCVV() {
        return String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }

}
