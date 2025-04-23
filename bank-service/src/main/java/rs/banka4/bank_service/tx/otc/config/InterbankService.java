package rs.banka4.bank_service.tx.otc.config;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.tx.data.OtcOffer;
import rs.banka4.bank_service.tx.data.PublicStock;

public interface InterbankService {
    @GET("/public-stock")
    Call<List<PublicStock>> getPublicStocks();

    @POST("/negotiations")
    Call<ForeignBankId> sendCreateOtc(@Body OtcOffer offer);

    @PUT("/negotiations/{routingNumber}/{id}")
    Call<ForeignBankId> sendUpdateOtc(
        @Body OtcOffer offer,
        @Path("routingNumber") long routingNumber,
        @Path("id") String id
    );
}
