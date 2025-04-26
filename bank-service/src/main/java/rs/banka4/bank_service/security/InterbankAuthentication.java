package rs.banka4.bank_service.security;

import java.util.List;
import org.springframework.lang.Contract;
import org.springframework.security.authentication.AbstractAuthenticationToken;

/** Authentication granted to other banks making requests to us. */
public class InterbankAuthentication extends AbstractAuthenticationToken {
    /* Authentication string granted to other banks. */
    public static final String OTHER_BANK_AUTHORITY = "OTHER_BANK";

    private long bankId;

    /** @param bankId ID of the other bank. */
    public InterbankAuthentication(long bankId) {
        super(List.of(() -> OTHER_BANK_AUTHORITY));
        this.bankId = bankId;
    }

    @Override
    public Void getCredentials() {
        return null;
    }

    /** @return The routing number of the other bank. */
    @Override
    @Contract("-> !null")
    public Long getPrincipal() {
        return bankId;
    }
}
