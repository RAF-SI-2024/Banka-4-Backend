package rs.banka4.bank_service.tx.otc.config;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.tx.data.OtcOffer;
import rs.banka4.bank_service.tx.data.PublicStock;

public interface InterbankService {
    @GET("/public-stock")
    Call<List<PublicStock>> getPublicStocks();

    @POST("/negotiations")
    Call<ForeignBankId> sendCreateOtc(@Body OtcOffer offer);
}
