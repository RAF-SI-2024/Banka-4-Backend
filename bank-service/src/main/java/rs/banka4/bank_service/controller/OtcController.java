package rs.banka4.bank_service.controller;

import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.bank_service.controller.docs.OtcApiDocumentation;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.trading.db.OtcMapper;
import rs.banka4.bank_service.domain.trading.db.dtos.OtcRequestCreateDto;
import rs.banka4.bank_service.domain.trading.db.dtos.OtcRequestDto;
import rs.banka4.bank_service.domain.trading.db.dtos.OtcRequestUpdateDto;
import rs.banka4.bank_service.exceptions.InterbankError;
import rs.banka4.bank_service.service.abstraction.ForeignBankService;
import rs.banka4.bank_service.service.abstraction.ListingService;
import rs.banka4.bank_service.service.abstraction.OtcRequestService;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;

@RestController
@RequestMapping("/stock/otc")
@RequiredArgsConstructor
@Slf4j
public class OtcController implements OtcApiDocumentation {
    private final OtcMapper otcMapper;
    private final OtcRequestService otcRequestService;
    private final ForeignBankService foreignBankService;
    private final ListingService listingService;

    @Override
    @GetMapping("/me")
    public ResponseEntity<Page<OtcRequestDto>> getMyRequests(
        int page,
        int size,
        Authentication auth
    ) {
        return getRequestHelper(page, size, auth, false);
    }

    @Override
    @GetMapping("/me/unread")
    public ResponseEntity<Page<OtcRequestDto>> getMyRequestsUnread(
        int page,
        int size,
        Authentication auth
    ) {
        return getRequestHelper(page, size, auth, true);
    }

    @Override
    @PatchMapping("/reject/{idId}/{routingNumber}")
    public ResponseEntity<Void> rejectOtcRequest(
        @PathVariable("idId") Long idId,
        @PathVariable("routingNumber") String routingNumber
    ) {
        ForeignBankId requestId = new ForeignBankId(idId, routingNumber);
        otcRequestService.rejectOtc(requestId);
        return ResponseEntity.ok()
            .build();
    }

    @Override
    @PatchMapping("/update/{idId}/{routingNumber}")
    public ResponseEntity<Void> updateOtcRequest(
        @RequestBody OtcRequestUpdateDto otcRequestUpdateDto,
        @PathVariable("idId") Long idId,
        @PathVariable("routingNumber") String routingNumber,
        Authentication auth
    ) {
        ForeignBankId id = new ForeignBankId(idId, routingNumber);

        final var ourAuth = (AuthenticatedBankUserAuthentication) auth;
        var myId =
            ourAuth.getPrincipal()
                .userId();
        otcRequestService.updateOtc(otcRequestUpdateDto, id, myId);
        return ResponseEntity.ok()
            .build();
    }

    @Override
    @PostMapping("/create")
    public ResponseEntity<Void> createOtcRequest(
        @RequestBody @Valid OtcRequestCreateDto otcRequestCreateDto,
        Authentication auth
    ) {
        final var ourAuth = (AuthenticatedBankUserAuthentication) auth;
        var myId =
            ourAuth.getPrincipal()
                .userId();
        otcRequestService.createOtc(otcRequestCreateDto, myId);
        return ResponseEntity.ok()
            .build();
    }

    @Override
    @PatchMapping("/accept/{idId}/{routingNumber}")
    public ResponseEntity<Void> acceptOtcRequest(
        @PathVariable("idId") Long idId,
        @PathVariable("routingNumber") String routingNumber,
        Authentication auth
    ) {
        ForeignBankId requestId = new ForeignBankId(idId, routingNumber);
        final var ourAuth = (AuthenticatedBankUserAuthentication) auth;
        var myId =
            ourAuth.getPrincipal()
                .userId();
        otcRequestService.acceptOtc(requestId, myId);
        return ResponseEntity.ok()
            .build();
    }

    private ResponseEntity<Page<OtcRequestDto>> getRequestHelper(
        int page,
        int size,
        Authentication auth,
        boolean unread
    ) {
        final var ourAuth = (AuthenticatedBankUserAuthentication) auth;
        var myId =
            ourAuth.getPrincipal()
                .userId();
        var requests =
            unread
                ? otcRequestService.getMyRequestsUnread(PageRequest.of(page, size), myId)
                : otcRequestService.getMyRequests(PageRequest.of(page, size), myId);

        Page<OtcRequestDto> dtoPage = requests.map(it -> {
            var latestPrice =
                listingService.getLatestPriceForStock(
                    it.getStock()
                        .getId()
                );
            try {
                return otcMapper.toOtcRequestDto(
                    it,
                    foreignBankService.getUsernameFor(it.getMadeBy())
                        .orElseGet(it.getMadeBy()::toString),
                    foreignBankService.getUsernameFor(it.getMadeFor())
                        .orElseGet(it.getMadeFor()::toString),
                    foreignBankService.getUsernameFor(it.getModifiedBy())
                        .orElseGet(it.getModifiedBy()::toString),
                    latestPrice
                );
            } catch (IOException error) {
                log.error("failed to resolve username for {}", it, error);
                throw new InterbankError();
            }
        });

        return ResponseEntity.ok(dtoPage);
    }


}
