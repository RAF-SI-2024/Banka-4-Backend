package rs.banka4.user_service.service.abstraction;

import rs.banka4.user_service.exceptions.InsufficientFunds;
import rs.banka4.user_service.models.AuthenticationEvent;

public interface PaymentVerificationProcessor {
    /**
     * Completes the transaction after successful verification.
     * This method must check for sufficient funds, update account balances,
     * and mark the transaction as REALIZED.
     *
     * @param event the verified authentication event containing the transaction ID
     * @throws InsufficientFunds if funds are not available at processing time
     */
    void process(AuthenticationEvent event);
}
