package rs.banka4.stock_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import rs.banka4.stock_service.domain.exchanges.dtos.ExchangeDto;

public interface ExchangeService {
    ResponseEntity<Page<ExchangeDto>> getAllExchanges(Pageable pageable);
}
