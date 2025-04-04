package rs.banka4.stock_service.config.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import rs.banka4.stock_service.domain.listing.dtos.ListingApiDto;
import rs.banka4.stock_service.domain.security.forex.dtos.ForexPairApiDto;
import rs.banka4.stock_service.domain.security.stock.dtos.StockInfoDto;


public interface AlphaVantageService {
    @Headers("Accept: application/json")
    @GET("query")
    Call<ForexPairApiDto> getForexPair(
        @Query("function") String function,
        @Query("from_currency") String symbol1,
        @Query("to_currency") String symbol2,
        @Query("apikey") String apiKey
    );

    @Headers("Accept: application/json")
    @GET("query")
    Call<StockInfoDto> getStockInfo(
        @Query("function") String function,
        @Query("symbol") String ticker,
        @Query("apikey") String apiKey
    );

    @Headers("Accept: application/json")
    @GET("query")
    Call<ListingApiDto> getListingInfo(
        @Query("function") String function,
        @Query("symbol") String symbol,
        @Query("apikey") String apiKey
    );
}
