package rs.banka4.stock_service.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.stock_service.domain.listing.dtos.ListingDto;
import rs.banka4.stock_service.service.abstraction.ListingService;

@Service
public class ListingServiceImpl implements ListingService {
    @Override
    public ResponseEntity<Page<ListingDto>> getListings(String securityType, Pageable pageable) {
        return null;
    }
}
