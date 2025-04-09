package rs.banka4.stock_service.config.clients;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import rs.banka4.rafeisen.common.dto.EmployeeResponseDto;
import rs.banka4.stock_service.domain.response.PaginatedResponse;

public interface UserServiceClient {

    @GET("employee/search/actuary-only")
    Call<PaginatedResponse<EmployeeResponseDto>> searchActuaryOnly(
        @Header("Authorization") String token,
        @Query("firstName") String firstName,
        @Query("lastName") String lastName,
        @Query("email") String email,
        @Query("position") String position,
        @Query("page") int page,
        @Query("size") int size
    );
}
