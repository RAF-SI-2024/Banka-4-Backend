package rs.banka4.user_service.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.CreateTransactionResponseDto;
import rs.banka4.user_service.dto.TransactionDto;
import rs.banka4.user_service.dto.PaymentStatus;
import rs.banka4.user_service.dto.requests.CreatePaymentDto;
import rs.banka4.user_service.dto.requests.VerificationRequestDto;
import rs.banka4.user_service.exceptions.*;
import rs.banka4.user_service.mapper.TransactionMapper;
import rs.banka4.user_service.models.*;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.TransactionRepository;
import rs.banka4.user_service.service.abstraction.PaymentService;
import rs.banka4.user_service.service.abstraction.PaymentVerificationProcessor;
import rs.banka4.user_service.utils.JwtUtil;
import rs.banka4.user_service.utils.specification.PaymentSpecification;
import rs.banka4.user_service.utils.specification.SpecificationCombinator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;
    private final JwtUtil jwtUtil;
    private final TransactionMapper transactionMapper;
    private final VerificationEventService verificationEventService;
    private final PaymentVerificationProcessorFactory verificationProcessorFactory;


    @Override
    @Transactional
    public ResponseEntity<CreateTransactionResponseDto> createPayment(Authentication authentication, CreatePaymentDto createPaymentDto) {
        String email = jwtUtil.extractUsername(authentication.getCredentials().toString());

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFound(email));

        Account fromAccount = accountRepository.findAccountByAccountNumber(createPaymentDto.fromAccount())
                .orElseThrow(AccountNotFound::new);

        Account toAccount = accountRepository.findAccountByAccountNumber(createPaymentDto.toAccount())
                .orElseThrow(AccountNotFound::new);

        if (!client.getAccounts().contains(fromAccount)) {
            throw new NotAccountOwner();
        }

        if (fromAccount.getBalance().subtract(createPaymentDto.fromAmount()).subtract(BigDecimal.ONE).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFunds();
        }

        UUID transactionId = UUID.randomUUID();
        Transaction transaction = Transaction.builder()
                .transactionNumber(transactionId.toString())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .from(new MonetaryAmount(createPaymentDto.fromAmount(), fromAccount.getCurrency()))
                .to(new MonetaryAmount(createPaymentDto.fromAmount(), toAccount.getCurrency()))
                .fee(new MonetaryAmount(BigDecimal.valueOf(1L), fromAccount.getCurrency()))
                .recipient(createPaymentDto.recipient())
                .paymentCode(createPaymentDto.paymentCode())
                .referenceNumber(createPaymentDto.referenceNumber())
                .paymentPurpose(createPaymentDto.paymentPurpose())
                .paymentDateTime(LocalDateTime.now())
                .status(PaymentStatus.IN_PROGRESS)
                .build();

        AuthenticationEvent authenticationEvent = verificationEventService.createVerificationEvent(transactionId.toString(), AuthenticationEventType.VERIFY_TRANSACTION);
        transactionRepository.save(transaction);

        return ResponseEntity.ok(new CreateTransactionResponseDto(authenticationEvent.getId().toString()));
    }

    @Override
    @Transactional
    public ResponseEntity<CreateTransactionResponseDto> createTransfer(Authentication authentication, CreatePaymentDto createPaymentDto) {
        String email = jwtUtil.extractUsername(authentication.getCredentials().toString());

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFound(email));

        Account fromAccount = accountRepository.findAccountByAccountNumber(createPaymentDto.fromAccount())
                .orElseThrow(AccountNotFound::new);

        Account toAccount = accountRepository.findAccountByAccountNumber(createPaymentDto.toAccount())
                .orElseThrow(AccountNotFound::new);

        if (!client.getAccounts().containsAll(List.of(fromAccount, toAccount))) {
            throw new NotAccountOwner();
        }

        if (fromAccount.getBalance().subtract(createPaymentDto.fromAmount()).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFunds();
        }

        UUID transactionId = UUID.randomUUID();
        Transaction transaction = Transaction.builder()
                .transactionNumber(transactionId.toString())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .from(new MonetaryAmount(createPaymentDto.fromAmount(), fromAccount.getCurrency()))
                .to(new MonetaryAmount(createPaymentDto.fromAmount(), toAccount.getCurrency()))
                .fee(new MonetaryAmount(BigDecimal.valueOf(0L), fromAccount.getCurrency()))
                .recipient(createPaymentDto.recipient())
                .paymentCode(createPaymentDto.paymentCode())
                .referenceNumber(createPaymentDto.referenceNumber())
                .paymentPurpose(createPaymentDto.paymentPurpose())
                .paymentDateTime(LocalDateTime.now())
                .status(PaymentStatus.IN_PROGRESS)
                .build();


        AuthenticationEvent authenticationEvent = verificationEventService.createVerificationEvent(transactionId.toString(), AuthenticationEventType.VERIFY_TRANSFER);
        transactionRepository.save(transaction);

        return ResponseEntity.ok(new CreateTransactionResponseDto(authenticationEvent.getId().toString()));

    }

    @Override
    public ResponseEntity<Page<TransactionDto>> getAllPaymentsForClient(String token, PaymentStatus paymentStatus, BigDecimal amount, LocalDate paymentDate, String accountNumber, PageRequest pageRequest) {
        SpecificationCombinator<Transaction> combinator = new SpecificationCombinator<>();

        if (paymentStatus != null) combinator.and(PaymentSpecification.hasStatus(paymentStatus));
        if (amount != null) combinator.and(PaymentSpecification.hasAmount(amount));
        if (paymentDate != null) combinator.and(PaymentSpecification.hasDate(paymentDate));

        if (accountNumber != null && !accountNumber.isEmpty()) {
            Account fromAccount = accountRepository.findAccountByAccountNumber(accountNumber)
                    .orElseThrow(AccountNotFound::new);

            combinator.and(PaymentSpecification.hasFromAccount(fromAccount));
            combinator.and(PaymentSpecification.hasToAccount(fromAccount));
        }

        Page<Transaction> transactions = transactionRepository.findAll(combinator.build(), pageRequest);
        Page<TransactionDto> transactionDtos = transactions.map(transactionMapper::toDto);

        return ResponseEntity.ok(transactionDtos);
    }

    @Override
    public ResponseEntity<TransactionDto> getTransactionById(String token, UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFound(transactionId.toString()));

        //TODO: check if user is owner of transaction

        return ResponseEntity.ok(transactionMapper.toDto(transaction));
    }


    @Override
    @Transactional
    public ResponseEntity<Void> verify(Authentication authentication, VerificationRequestDto verificationRequestDto) {
        // First verify the TOTP and update the verification event.
        AuthenticationEvent event = verificationEventService.verifyEvent(authentication, verificationRequestDto);

        // Delegate the funds transfer logic to the appropriate processor.
        PaymentVerificationProcessor processor = verificationProcessorFactory.getProcessor(event.getType());
        processor.process(event);

        return ResponseEntity.ok().build();
    }

}