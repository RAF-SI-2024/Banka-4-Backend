package rs.banka4.bank_service.tx.otc.config;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import rs.banka4.bank_service.tx.data.PublicStock;

public interface InterbankService {
    @GET("/public-stock")
    Call<List<PublicStock>> getPublicStocks();
}
