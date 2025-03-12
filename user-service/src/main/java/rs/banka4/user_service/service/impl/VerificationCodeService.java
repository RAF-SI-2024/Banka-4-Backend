package rs.banka4.user_service.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.domain.auth.db.VerificationCode;
import rs.banka4.user_service.exceptions.user.VerificationCodeExpiredOrInvalid;
import rs.banka4.user_service.repositories.VerificationCodeRepository;

@Service
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final LocalDateTime expiration =
        LocalDateTime.now()
            .plusDays(7);

    public VerificationCodeService(VerificationCodeRepository verificationCodeRepository) {
        this.verificationCodeRepository = verificationCodeRepository;
    }

    public VerificationCode createVerificationCode(String email) {
        String code =
            UUID.randomUUID()
                .toString();

        VerificationCode verificationCode =
            VerificationCode.builder()
                .code(code)
                .email(email)
                .expirationDate(expiration)
                .build();
        return verificationCodeRepository.save(verificationCode);
    }

    public Optional<VerificationCode> validateVerificationCode(String code) {
        Optional<VerificationCode> optionalCode = verificationCodeRepository.findByCode(code);
        if (optionalCode.isPresent()) {
            VerificationCode verificationCode = optionalCode.get();
            // Code is invalid if already used or expired
            if (
                verificationCode.isUsed()
                    || verificationCode.getExpirationDate()
                        .isBefore(LocalDateTime.now())
            ) {
                throw new VerificationCodeExpiredOrInvalid();
            }
            return Optional.of(verificationCode);
        }
        return Optional.empty();
    }

    public void markCodeAsUsed(VerificationCode verificationCode) {
        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);
    }

    public Optional<VerificationCode> findByEmail(String email) {
        return verificationCodeRepository.findByEmail(email);
    }

    public Optional<VerificationCode> findByCode(String code) {
        return verificationCodeRepository.findByCode(code);
    }
}
