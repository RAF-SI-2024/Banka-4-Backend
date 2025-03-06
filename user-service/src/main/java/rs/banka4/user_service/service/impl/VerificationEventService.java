package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.requests.VerificationRequestDto;
import rs.banka4.user_service.exceptions.NotFound;
import rs.banka4.user_service.exceptions.NotValidTotpException;
import rs.banka4.user_service.models.AuthenticationEvent;
import rs.banka4.user_service.models.AuthenticationEventType;
import rs.banka4.user_service.repositories.AuthenticationEventRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationEventService {

    private final TotpService totpService;
    private final AuthenticationEventRepository authenticationEventRepository;

    /**
     * Verifies the provided TOTP code for a specific authentication event, marking it as verified.
     *
     * @param authentication the current user authentication
     * @param verificationRequestDto the DTO containing the event ID and TOTP code
     * @return the updated AuthenticationEvent after verification
     * @throws NotValidTotpException if the TOTP code is invalid
     * @throws NotFound if no event with the given ID exists
     */
    public AuthenticationEvent verifyEvent(Authentication authentication, VerificationRequestDto verificationRequestDto) {
        UUID eventId;
        try {
            eventId = UUID.fromString(verificationRequestDto.eventId());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid event ID format", e);
        }

        String authorizationHeader = authentication.getCredentials().toString();
        if (!totpService.validate(authorizationHeader, verificationRequestDto.code())) {
            throw new NotValidTotpException();
        }

        AuthenticationEvent event = authenticationEventRepository.findById(eventId)
                .orElseThrow(NotFound::new);
        event.setDidAuthenticate(true);
        return authenticationEventRepository.save(event);
    }


    /**
     * Creates and saves a new verification event with the provided ID and type.
     *
     * @param eventId the UUID to assign to the event
     * @param type the type of the event (e.g. VERIFY_TRANSACTION)
     * @return the created AuthenticationEvent
     */
    public AuthenticationEvent createVerificationEvent(UUID eventId, AuthenticationEventType type) {
        AuthenticationEvent event = new AuthenticationEvent();
        event.setId(eventId);
        event.setType(type);
        event.setDidAuthenticate(false);
        return authenticationEventRepository.save(event);
    }


}
