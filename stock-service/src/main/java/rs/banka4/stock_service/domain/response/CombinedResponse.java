package rs.banka4.stock_service.domain.response;


public record CombinedResponse(
    EmployeeResponseDto user,
    ActuaryInfoDto actuary
) {
}
