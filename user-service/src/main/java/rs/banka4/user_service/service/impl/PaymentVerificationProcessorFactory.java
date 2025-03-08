package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.models.AuthenticationEventType;
import rs.banka4.user_service.service.abstraction.PaymentVerificationProcessor;

@Service
@RequiredArgsConstructor
public class PaymentVerificationProcessorFactory {

    private final PaymentTransactionVerificationProcessor paymentProcessor;
    private final TransferVerificationProcessor transferProcessor;

    public PaymentVerificationProcessor getProcessor(AuthenticationEventType eventType) {
        return switch (eventType) {
            case VERIFY_TRANSACTION -> paymentProcessor;
            case VERIFY_TRANSFER -> transferProcessor;
            default -> throw new IllegalArgumentException("Unsupported event type: " + eventType);
        };
    }
}
