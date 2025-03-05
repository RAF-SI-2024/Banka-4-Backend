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
import rs.banka4.user_service.dto.requests.CreateTransactionDto;
import rs.banka4.user_service.service.abstraction.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {


    @Override
    public ResponseEntity<TransactionDto> createPayment(CreateTransactionDto createTransactionDto){
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Page<TransactionDto>> getPaymentsForClient(String token, PaymentStatus aymentStatus, BigDecimal amount, LocalDate paymentDate, PageRequest pageRequest){
        TransactionDto transactionDto1 = new TransactionDto(
        "e2a1f6f3-9f74-4b8a-bc9a-2f3a5c6d7e8f",
            "1265463698391",
            "102-39443942389",
            "102-394438340549",
            BigDecimal.valueOf(500.00),
            "EUR",
            BigDecimal.valueOf(600.00),
            "USD",
            BigDecimal.valueOf(5.0),
            "EUR",
            "Pera Perić",
            "289",
            "1176926",
            "za privatni čas",
            LocalDateTime.now(),
            PaymentStatus.REALIZED
        );

        TransactionDto transactionDto2 = new TransactionDto(
                "a3b2f5d6-c27e-44a2-b85a-b719a88b2b6c",
                "1928374650213",
                "102-39483947329",
                "102-394487234534",
                BigDecimal.valueOf(1000.00),
                "USD",
                BigDecimal.valueOf(1200.00),
                "EUR",
                BigDecimal.valueOf(10.0),
                "USD",
                "Jovan Jovanović",
                "123",
                "9348472",
                "payment for services",
                LocalDateTime.now(),
                PaymentStatus.REALIZED
        );

        List<TransactionDto> payments = List.of(transactionDto1, transactionDto2);
        Page<TransactionDto> paymentPage = new PageImpl<>(payments, pageRequest, payments.size());

        return ResponseEntity.ok(paymentPage);
    }
}
