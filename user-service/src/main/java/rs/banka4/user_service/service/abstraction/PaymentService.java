package rs.banka4.user_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import rs.banka4.user_service.dto.TransactionDto;
import rs.banka4.user_service.dto.PaymentStatus;
import rs.banka4.user_service.dto.requests.CreatePaymentDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PaymentService {
    ResponseEntity<TransactionDto> createPayment(CreatePaymentDto createPaymentDto);
    ResponseEntity<Page<TransactionDto>> getPaymentsForClient(String token, PaymentStatus paymentStatus, BigDecimal amount, LocalDate paymentDate, PageRequest pageRequest);
}
