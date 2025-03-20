package rs.banka4.stock_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import rs.banka4.stock_service.domain.listing.dtos.ListingDto;

public interface ListingService {
    ResponseEntity<Page<ListingDto>> getListings(String securityType, Pageable pageable);
}
