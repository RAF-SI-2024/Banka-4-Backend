package rs.banka4.bank_service.exceptions;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class OtcNotFoundException extends BaseApiException {
    public OtcNotFoundException(ForeignBankId id) {
        super(HttpStatus.NOT_FOUND, Map.of("id", id));
    }
}
