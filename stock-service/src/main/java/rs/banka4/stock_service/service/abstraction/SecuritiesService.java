package rs.banka4.stock_service.service.abstraction;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.stock_service.domain.security.SecurityDto;
import rs.banka4.stock_service.domain.security.responses.SecurityOwnershipResponse;


public interface SecuritiesService {
    // Object is StockDto, FutureDto or ForexDto
    ResponseEntity<Page<SecurityDto>> getSecurities(
        String securityType,
        String name,
        Pageable pageable
    );

    List<SecurityOwnershipResponse> getMySecurities(Authentication authentication);

}
