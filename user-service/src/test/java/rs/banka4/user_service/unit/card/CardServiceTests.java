package rs.banka4.user_service.unit.card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.account.db.AccountType;
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.db.CardType;
import rs.banka4.user_service.domain.card.dtos.CreateAuthorizedUserDto;
import rs.banka4.user_service.domain.card.dtos.CreateCardDto;
import rs.banka4.user_service.domain.card.mapper.CardMapper;
import rs.banka4.user_service.domain.user.Gender;
import rs.banka4.user_service.domain.user.employee.db.Employee;
import rs.banka4.user_service.exceptions.authenticator.NotValidTotpException;
import rs.banka4.user_service.exceptions.card.AuthorizedUserNotAllowed;
import rs.banka4.user_service.exceptions.card.CardLimitExceededException;
import rs.banka4.user_service.exceptions.card.DuplicateAuthorizationException;
import rs.banka4.user_service.generator.CardObjectMother;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.CardRepository;
import rs.banka4.user_service.service.impl.CardServiceImpl;
import rs.banka4.user_service.service.impl.TotpService;
import rs.banka4.user_service.utils.JwtUtil;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//@ExtendWith(MockitoExtension.class)
//class CardServiceTests {
//
//    @Mock private CardRepository cardRepository;
//    @Mock private AccountRepository accountRepository;
//    @Mock private TotpService totpService;
//    @Mock private JwtUtil jwtUtil;
//    @InjectMocks private CardServiceImpl cardService;
//
//    private final Account businessAccount = Account.builder()
//            .accountType(AccountType.DOO)
//            .accountNumber("BUSINESS_ACC_123")
//            .build();
//
//    private final Account personalAccount = Account.builder()
//            .accountType(AccountType.STANDARD)
//            .accountNumber("PERSONAL_ACC_456")
//            .build();
//
//    @Test
//    void createAuthorizedCard_ValidBusinessAccountWithNewUser_Success() {
//        CreateCardDto dto = new CreateCardDto(
//                "BUSINESS_ACC_123",
//                new CreateAuthorizedUserDto("John", "Doe", LocalDate.now(),
//                        Gender.MALE, "john@biz.com", "+123456", "Address"),
//                "123456"
//        );
//
//        when(accountRepository.findAccountByAccountNumber(any())).thenReturn(Optional.of(businessAccount));
//        when(totpService.validate(any(), any())).thenReturn(true);
//        when(cardRepository.existsByAccountAndAuthorizedUserEmail(any(), any())).thenReturn(false);
//
//        cardService.createAuthorizedCard(mock(Authentication.class), dto);
//
//        verify(cardRepository).save(any());
//    }
//
//    @Test
//    void createAuthorizedCard_InvalidTotp_ThrowsException() {
//        CreateCardDto dto = new CreateCardDto("ACC_123", null, "wrong");
//        when(totpService.validate(any(), any())).thenReturn(false);
//
//        assertThrows(NotValidTotpException.class,
//                () -> cardService.createAuthorizedCard(mock(Authentication.class), dto));
//    }
//
//    // Test Cases for createEmployeeCard
//    @Test
//    void createEmployeeCard_FirstBusinessCardWithoutAuth_Success() {
//        when(cardRepository.countByAccount(businessAccount)).thenReturn(0);
//
//        cardService.createEmployeeCard(
//                new CreateCardDto("BUSINESS_ACC_123", null, null),
//                businessAccount
//        );
//
//        verify(cardRepository).save(argThat(card ->
//                card.getAuthorizedUser() == null
//        ));
//    }
//
//    @Test
//    void createEmployeeCard_PersonalAccountWithAuth_ThrowsException() {
//        CreateCardDto dto = new CreateCardDto(
//                "PERSONAL_ACC_456",
//                new CreateAuthorizedUserDto(),
//        null
//        );
//
//        assertThrows(AuthorizedUserNotAllowed.class,
//                () -> cardService.createEmployeeCard(dto, personalAccount));
//    }
//
//    @Test
//    void createEmployeeCard_BusinessAccountDuplicateUser_ThrowsException() {
//        CreateCardDto dto = new CreateCardDto(
//                "BUSINESS_ACC_123",
//                new  CreateAuthorizedUserDto(),
//        null
//        );
//
//        when(cardRepository.existsByAccountAndAuthorizedUserEmail(any(), any())).thenReturn(true);
//
//        assertThrows(DuplicateAuthorizationException.class,
//                () -> cardService.createEmployeeCard(dto, businessAccount));
//    }
//
//    @Test
//    void createEmployeeCard_PersonalAccountLimitExceeded_ThrowsException() {
//        when(cardRepository.countByAccount(personalAccount)).thenReturn(2);
//
//        assertThrows(CardLimitExceededException.class,
//                () -> cardService.createEmployeeCard(
//                        new CreateCardDto("PERSONAL_ACC_456", null, null),
//                        personalAccount
//                ));
//    }
//}
