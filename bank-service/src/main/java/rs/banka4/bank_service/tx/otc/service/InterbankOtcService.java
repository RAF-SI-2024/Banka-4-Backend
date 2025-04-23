package rs.banka4.bank_service.tx.otc.service;

import java.util.List;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.tx.data.OtcOffer;
import rs.banka4.bank_service.tx.data.PublicStock;

public interface InterbankOtcService {
    List<PublicStock> sendPublicStocks();

    List<PublicStock> fetchPublicStocks();

    ForeignBankId createOtc(OtcOffer offer);

    void sendCreateOtc(OtcOffer offer);

    void updateOtc(OtcOffer offer, ForeignBankId id);

    void sendUpdateOtc(OtcOffer offer, ForeignBankId id);
}
