package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.TransactionDto;
import rs.banka4.user_service.dto.PaymentStatus;
import rs.banka4.user_service.dto.requests.CreatePaymentDto;
import rs.banka4.user_service.service.abstraction.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {


    @Override
    public ResponseEntity<TransactionDto> createPayment(CreatePaymentDto createPaymentDto){
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Page<TransactionDto>> getPaymentsForClient(String token, PaymentStatus aymentStatus, BigDecimal amount, LocalDate paymentDate, PageRequest pageRequest){
        TransactionDto transactionDto = new TransactionDto(
                "e2a1f6f3-9f74-4b8a-bc9a-2f3a5c6d7e8f",
                "1265463698391",
                "102-39443942389",
                "102-394438340549",
                BigDecimal.ONE,
                "EUR",
                "Pera Perić",
                "289",
                "1176926",
                "za privatni čas",
                LocalDateTime.now(),
                PaymentStatus.REALIZED
        );

        TransactionDto transactionDto2 = new TransactionDto(
                "e2a1f6f3-9f74-4b8a-bc9a-2f3a5c6d7e8e",
                "1265463698391",
                "102-39443942399",
                "102-394438340549",
                BigDecimal.TWO,
                "EUR",
                "Pera Perić",
                "289",
                "1176926",
                "za privatni čas",
                LocalDateTime.now(),
                PaymentStatus.REALIZED
        );

        List<TransactionDto> payments = List.of(transactionDto, transactionDto2);
        Page<TransactionDto> paymentPage = new PageImpl<>(payments, pageRequest, payments.size());

        return ResponseEntity.ok(paymentPage);
    }
}
