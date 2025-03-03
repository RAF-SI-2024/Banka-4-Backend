package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import rs.banka4.user_service.dto.CompanyDto;
import rs.banka4.user_service.models.Currency;

import java.math.BigDecimal;

@Schema(description = "Data Transfer Object representing an account")
public record CreateAccountDto (

        @NotNull
        @Schema(description = "Client owner of the account", example = "{\n" +
                "        \"clientId\": null,\n" +
                "        \"name\": \"...\",\n" +
                "        \"surname\": \"...\",\n" +
                "        \"dateOfBirth\": \"...\",\n" +
                "        \"gender\": \"...\",\n" +
                "        \"email\": \"...\",\n" +
                "        \"phone\": \"...\",\n" +
                "        \"address\": \"...\"\n" +
                "    }")
        CreateClientDto client,

        @Null
        @Schema(description = "Currency code associated with this account", example = "{\n" +
                "        \"companyId\": null,\n" +
                "        \"name\": \"...\",\n" +
                "        \"registrationNumber\": \"...\",\n" +
                "        \"taxNumber\": \"...\",\n" +
                "        \"address\": \"...\"\n" +
                "    }")
        CompanyDto company,

        @NotNull(message = "Balance cannot be null")
        @Schema(description = "Initial balance", example = "1000.00")
        BigDecimal availableBalance,

        @NotNull(message = "Currency code cannot be null")
        @Schema(description = "Currency code associated with this account", example = "RSD")
        Currency.Code currency
) { }
