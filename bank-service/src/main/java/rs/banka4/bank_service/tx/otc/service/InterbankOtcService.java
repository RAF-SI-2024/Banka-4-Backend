package rs.banka4.bank_service.tx.otc.service;

import java.util.List;
import rs.banka4.bank_service.tx.data.PublicStock;

public interface InterbankOtcService {
    List<PublicStock> sendPublicStocks();

    List<PublicStock> fetchPublicStocks();
}
