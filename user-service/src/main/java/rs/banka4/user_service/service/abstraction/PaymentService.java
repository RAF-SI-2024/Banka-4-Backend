package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.user_service.dto.CreateTransactionResponseDto;
import rs.banka4.user_service.dto.TransactionDto;
import rs.banka4.user_service.dto.PaymentStatus;
import rs.banka4.user_service.dto.requests.CreatePaymentDto;
import rs.banka4.user_service.dto.requests.CreateTransactionDto;
import rs.banka4.user_service.dto.requests.VerificationRequestDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface PaymentService {
    ResponseEntity<CreateTransactionResponseDto> createPayment(Authentication authentication, CreatePaymentDto createPaymentDto);
    ResponseEntity<CreateTransactionResponseDto> createTransfer(Authentication authentication, CreatePaymentDto createPaymentDto);
    ResponseEntity<Page<TransactionDto>> getAllPaymentsForClient(String token, PaymentStatus paymentStatus, BigDecimal amount, LocalDate paymentDate,  String accountNumber, PageRequest pageRequest);
    ResponseEntity<TransactionDto> getTransactionById(String token, UUID transactionId);
    ResponseEntity<Void> verify(Authentication authentication, VerificationRequestDto verificationRequestDto);

}
