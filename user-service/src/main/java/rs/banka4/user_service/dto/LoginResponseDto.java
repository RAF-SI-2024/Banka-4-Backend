package rs.banka4.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login response dto")
public record LoginResponseDto(
        @Schema(description = "Access token",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiZW1wbG95ZWUiLCJpZCI6Ijk1NDM3MDdkLTcxNjQtNGI4My1iNzMxLTVlMGExZGExYzJmOSIsInN1YiI6Im1hcmtvdmljbWFya29AZXhhbXBsZS5jb20iLCJpYXQiOjE3NDA2NjQ0MTQsImV4cCI6MTc0MDY2NDU5NH0.COm2g-SUtLF5v58yKduivRfm5PFOqgFRAoGvnjactUw",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String accessToken,
        @Schema(description = "Refresh token",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYXJrb3ZpY21hcmtvQGV4YW1wbGUuY29tIiwiaWF0IjoxNzQwNjY0NDE0LCJleHAiOjE3NDEyNjkyMTR9.yoAY9A0OdOef8EoC3cJlArTQRYigjKkMONLpZ0w1DRQ",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String refreshToken
) {}