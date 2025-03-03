package rs.banka4.user_service.dto.requests;

import rs.banka4.user_service.models.ActivityCode;

public record CreateCompanyDto(
        String name,
        String tin,
        String address,
        ActivityCode activityCode
) {
}
