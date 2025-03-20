package rs.banka4.stock_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rs.banka4.stock_service.controller.docs.ListingApiDocumentation;
import rs.banka4.stock_service.domain.listing.dtos.ListingDto;
import rs.banka4.stock_service.service.abstraction.ListingService;

@RestController
@RequestMapping("/listings")
@RequiredArgsConstructor
public class ListingController implements ListingApiDocumentation {

    // Note: The @Primary annotation in the mock service should be deleted.
    private final ListingService listingService;

    @Override
    @GetMapping
    public ResponseEntity<Page<ListingDto>> getListings(
        @RequestParam(required = false) String securityType,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return listingService.getListings(securityType, PageRequest.of(page, size));
    }
}
