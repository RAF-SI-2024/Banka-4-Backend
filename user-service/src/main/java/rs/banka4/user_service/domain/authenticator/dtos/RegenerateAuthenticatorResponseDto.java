package rs.banka4.user_service.domain.authenticator.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Newly generated token used for setting up 2FA TOTP Authenticator")
public record RegenerateAuthenticatorResponseDto(
    @Schema(
        description = "Newly generated TOTP otpauth, used for mobile clients to quickly setup TOTP with the given secret. QR code can be generated from this URI allowing the user to scan the QR code to create an authenticator.",
        example = "otpauth://totp/RAFeisen:johndoe@gmail.com?secret=BNYA5XGJWXUZ57AUG26COVKIDPAZPVAP&issuer=RAFeisen&algorithm=SHA1&digits=6&period=30"
    ) String url,
    @Schema(
        description = "Token secret used to setup a TOTP code in an authenticator app.",
        example = "bnya 5xgj wxuz 57au g26c oxki dpmz pvap"
    ) String tokenSecret
) {
}
