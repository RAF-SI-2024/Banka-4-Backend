package rs.banka4.user_service.config.clients;


import java.util.UUID;
import retrofit2.Call;
import retrofit2.http.*;
import rs.banka4.user_service.domain.user.employee.dtos.ActuaryPayloadDto;

public interface StockServiceClient {
    @POST("actuaries/register")
    Call<Void> registerActuary(
        @Header("Authorization") String authorization,
        @Body ActuaryPayloadDto actuaryPayloadDto
    );

    @PUT("actuaries/update/{id}")
    Call<Void> updateActuary(
        @Header("Authorization") String authorization,
        @Path("id") UUID id,
        @Body ActuaryPayloadDto actuaryPayloadDto
    );
}
