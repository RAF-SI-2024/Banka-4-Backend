package rs.banka4.bank_service.service.abstraction;

import java.io.IOException;
import java.util.Optional;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.tx.data.UserInformation;

/**
 * A service for all things related to resolving foreign bank identifiers.
 */
public interface ForeignBankService {
    Optional<UserInformation> getUserInfoFor(ForeignBankId foreignBankId) throws IOException;

    default Optional<String> getUsernameFor(ForeignBankId foreignBankId) throws IOException {
        return getUserInfoFor(foreignBankId).map(UserInformation::displayName);
    }

    default Optional<String> getBankNameFor(ForeignBankId foreignBankId) throws IOException {
        return getUserInfoFor(foreignBankId).map(UserInformation::bankDisplayName);
    }
}
