package rs.banka4.stock_service.service.abstraction;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.stock_service.domain.actuaries.db.dto.ActuaryPayloadDto;
import rs.banka4.stock_service.domain.response.CombinedResponse;
import rs.banka4.stock_service.domain.response.LimitPayload;

public interface ActuaryService {
    void createNewActuary(ActuaryPayloadDto dto);

    void changeActuaryDetails(UUID actuaryId, ActuaryPayloadDto dto);

    void updateLimit(UUID actuaryId, LimitPayload dto);

    ResponseEntity<Page<CombinedResponse>> search(
        Authentication auth,
        String firstName,
        String lastName,
        String email,
        String position,
        int page,
        int size
    );

    void resetUsedLimit(UUID actuaryId);
}
