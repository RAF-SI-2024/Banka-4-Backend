package rs.banka4.stock_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import rs.banka4.stock_service.domain.security.SecurityDto;


public interface SecuritiesService {
    // Object is StockDto, FutureDto or ForexDto
    ResponseEntity<Page<SecurityDto>> getSecurities(
        String securityType,
        String name,
        Pageable pageable
    );
}
