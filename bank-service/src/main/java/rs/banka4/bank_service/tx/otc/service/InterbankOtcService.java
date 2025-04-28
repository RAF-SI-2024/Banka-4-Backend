package rs.banka4.bank_service.tx.otc.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.trading.dtos.PublicStocksDto;
import rs.banka4.bank_service.tx.data.OtcNegotiation;
import rs.banka4.bank_service.tx.data.OtcOffer;
import rs.banka4.bank_service.tx.data.PublicStock;

public interface InterbankOtcService {
    List<PublicStock> sendPublicStocks();

    List<PublicStock> fetchPublicStocks();

    List<PublicStocksDto> getPublicStocks(Pageable pageable, String token);

    ForeignBankId createOtc(OtcOffer offer);

    void sendCreateOtc(OtcOffer offer);

    void updateOtc(OtcOffer offer, ForeignBankId id);

    void sendUpdateOtc(OtcOffer offer, ForeignBankId id, long routingNumber);

    OtcNegotiation getOtcNegotiation(ForeignBankId id);

    OtcNegotiation sendGetOtcNegotiation(ForeignBankId id);

    void closeNegotiation(ForeignBankId id);

    void sendCloseNegotiation(ForeignBankId id, long routingNumber);

    void acceptNegotiation(ForeignBankId id);

    void sendAcceptNegotiation(ForeignBankId id, long routingNumber);
}
