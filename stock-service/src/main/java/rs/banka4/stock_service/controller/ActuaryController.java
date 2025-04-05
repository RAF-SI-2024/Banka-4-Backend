package rs.banka4.stock_service.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.stock_service.controller.docs.ActuaryApiDocumentation;
import rs.banka4.stock_service.domain.actuaries.db.dto.ActuaryPayloadDto;
import rs.banka4.stock_service.domain.response.CombinedResponse;
import rs.banka4.stock_service.domain.response.LimitPayload;
import rs.banka4.stock_service.service.impl.ActuaryServiceImpl;

@RestController
@RequestMapping("/actuaries")
@RequiredArgsConstructor
public class ActuaryController implements ActuaryApiDocumentation {
    private final ActuaryServiceImpl actuaryService;

    @Override
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid ActuaryPayloadDto dto) {
        actuaryService.createNewActuary(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .build();
    }

    @Override
    @PutMapping("/update/{id}")
    public ResponseEntity<Void> update(
        @PathVariable UUID id,
        @RequestBody @Valid ActuaryPayloadDto dto
    ) {
        actuaryService.changeActuaryDetails(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .build();
    }

    @Override
    @PostMapping("/search")
    public ResponseEntity<Page<CombinedResponse>> search(
        @RequestParam(required = false) String firstName,
        @RequestParam(required = false) String lastName,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String position,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Authentication auth
    ) {

        return actuaryService.search(auth, firstName, lastName, email, position, page, size);
    }

    @PutMapping("/limit/{actuaryId}")
    public ResponseEntity<Void> updateLimit(
        @PathVariable UUID actuaryId,
        @RequestBody @Valid LimitPayload dto
    ) {
        actuaryService.updateLimit(actuaryId, dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .build();
    }

    @Override
    @PutMapping("/limit/reset/{actuaryId}")
    public ResponseEntity<Void> resetUsedLimit(@PathVariable UUID actuaryId) {
        actuaryService.resetUsedLimit(actuaryId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .build();
    }
}
