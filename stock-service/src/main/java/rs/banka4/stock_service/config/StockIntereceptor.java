package rs.banka4.stock_service.config;

import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class StockIntereceptor implements Interceptor {
    private static final String BASE_URL = "https://www.alphavantage.co/";

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        HttpUrl newUrl =
            HttpUrl.parse(BASE_URL)
                .newBuilder()
                .encodedPath(
                    originalRequest.url()
                        .encodedPath()
                )
                .encodedQuery(
                    originalRequest.url()
                        .encodedQuery()
                )
                .build();

        Request newRequest =
            originalRequest.newBuilder()
                .url(newUrl)
                .build();

        return chain.proceed(newRequest);
    }
}
