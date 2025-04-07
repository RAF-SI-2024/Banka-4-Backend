package rs.banka4.stock_service.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.rafeisen.common.security.UserType;
import rs.banka4.stock_service.controller.docs.ListingApiDocumentation;
import rs.banka4.stock_service.domain.listing.dtos.ListingDetailsDto;
import rs.banka4.stock_service.domain.listing.dtos.ListingFilterDto;
import rs.banka4.stock_service.domain.listing.dtos.ListingInfoDto;
import rs.banka4.stock_service.domain.listing.dtos.OptionDto;
import rs.banka4.stock_service.domain.listing.dtos.PriceChangeDto;
import rs.banka4.stock_service.service.abstraction.ListingService;

@RestController
@RequestMapping("/listings")
@RequiredArgsConstructor
public class ListingController implements ListingApiDocumentation {

    // Note: The @Primary annotation in the mock service should be deleted.
    private final ListingService listingService;

    @Override
    @GetMapping
    public Page<ListingInfoDto> getListings(
        @ModelAttribute ListingFilterDto filter,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Authentication auth
    ) {
        final var ourAuth = (AuthenticatedBankUserAuthentication) auth;
        return listingService.getListings(
            filter,
            PageRequest.of(page, size),
            ourAuth.getPrincipal()
                .userType()
                .equals(UserType.CLIENT)
        );
    }

    @GetMapping("/{securityId}/priceChange")
    public ResponseEntity<List<PriceChangeDto>> getPriceChanges(@PathVariable UUID securityId) {
        return new ResponseEntity<>(listingService.getPriceChanges(securityId), HttpStatus.OK);
    }

    @GetMapping("/{securityId}")
    public ResponseEntity<ListingDetailsDto> getListingDetails(@PathVariable UUID securityId) {
        return new ResponseEntity<>(listingService.getListingDetails(securityId), HttpStatus.OK);
    }

    @GetMapping("/options/{stockId}")
    public ResponseEntity<List<OptionDto>> getListingOptions(
        @PathVariable UUID stockId,
        @RequestParam OffsetDateTime settlementDate
    ) {
        return new ResponseEntity<>(
            listingService.getOptionsWithSettlementDateForStock(stockId, settlementDate),
            HttpStatus.OK
        );
    }
}
