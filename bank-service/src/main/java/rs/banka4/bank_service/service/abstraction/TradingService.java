package rs.banka4.bank_service.service.abstraction;

import java.util.UUID;
import rs.banka4.bank_service.domain.options.db.Option;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.trading.db.OtcRequest;
import rs.banka4.rafeisen.common.dto.AccountNumberDto;

public interface TradingService {
    void sendPremiumAndGetOption(
        AccountNumberDto buyer,
        AccountNumberDto seller,
        OtcRequest otcRequest
    );

    void buyOption(Option o, UUID userId, String userAccount, int amount);

    void usePutOption(Option o, UUID userId, String userAccount, int amount);

    void useCallOptionFromExchange(Option o, UUID userId, String userAccount, int amount);

    void useCallOptionFromOtc(
        Option o,
        ForeignBankId buyerId,
        ForeignBankId sellerId,
        String buyerAccount,
        String sellerAccount,
        int amount
    );
}
