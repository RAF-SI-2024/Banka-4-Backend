package rs.banka4.user_service.generator;

import java.time.LocalDateTime;
import java.util.UUID;
import rs.banka4.user_service.domain.auth.db.VerificationCode;

public class VerificationCodeObjectMother {

    public static VerificationCode createValidVerificationCode(String email) {
        return VerificationCode.builder()
            .code(
                UUID.randomUUID()
                    .toString()
            )
            .expirationDate(
                LocalDateTime.now()
                    .plusDays(1)
            )
            .used(false)
            .email(email)
            .build();
    }

    public static VerificationCode createExpiredVerificationCode(String email) {
        return VerificationCode.builder()
            .code(
                UUID.randomUUID()
                    .toString()
            )
            .expirationDate(
                LocalDateTime.now()
                    .minusDays(1)
            )
            .used(false)
            .email(email)
            .build();
    }

    public static VerificationCode createUsedVerificationCode(String email) {
        return VerificationCode.builder()
            .code(
                UUID.randomUUID()
                    .toString()
            )
            .expirationDate(
                LocalDateTime.now()
                    .plusDays(1)
            )
            .used(true)
            .email(email)
            .build();
    }
}
