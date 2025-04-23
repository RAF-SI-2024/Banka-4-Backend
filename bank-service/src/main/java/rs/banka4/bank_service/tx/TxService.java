package rs.banka4.bank_service.tx;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service tasked with creating and submitting transactions for execution.
 */
@Service
@RequiredArgsConstructor
public class TxService {
    private final TxExecutor txExecutor;
}
