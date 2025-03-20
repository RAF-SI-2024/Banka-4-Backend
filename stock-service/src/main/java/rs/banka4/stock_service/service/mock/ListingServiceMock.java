package rs.banka4.stock_service.service.mock;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.stock_service.domain.listing.dtos.ListingDto;
import rs.banka4.stock_service.service.abstraction.ListingService;
import rs.banka4.stock_service.service.mock.generators.ListingObjectMother;

@Service
@Primary
public class ListingServiceMock implements ListingService {

    @Override
    public ResponseEntity<Page<ListingDto>> getListings(String securityType, Pageable pageable) {
        List<ListingDto> dtos = new ArrayList<>();
        dtos.add(ListingObjectMother.generateForexListing());
        dtos.add(ListingObjectMother.generateFutureListing());
        dtos.add(ListingObjectMother.generateListingDto());
        dtos.add(ListingObjectMother.generateStockListing());
        Page<ListingDto> page = new PageImpl<>(dtos, pageable, 4);
        return ResponseEntity.ok(page);
    }

    /*
     * @Override public ResponseEntity<Page<CardDto>> clientSearchCards( String accountNumber,
     * Pageable pageable ) { List<CardDto> dtos = new ArrayList<>();
     * dtos.add(CardObjectMother.generateCardDto()); dtos.add(CardObjectMother.generateCardDto());
     * dtos.add(CardObjectMother.generateCardDto()); Page<CardDto> page = new PageImpl<>(dtos,
     * pageable, 3); return ResponseEntity.ok(page); }
     */
}
