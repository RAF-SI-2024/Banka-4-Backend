package rs.banka4.stock_service.service.mock;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.stock_service.domain.exchanges.dtos.ExchangeDto;
import rs.banka4.stock_service.service.abstraction.ExchangeService;
import rs.banka4.stock_service.service.mock.generators.ExchangeObjectMother;

@Primary
@Service
public class ExchangeServiceMock implements ExchangeService {

    @Override
    public ResponseEntity<Page<ExchangeDto>> getAllExchanges(Pageable pageable) {
        List<ExchangeDto> dtos = new ArrayList<>();
        dtos.add(ExchangeObjectMother.generateNYSEExchangeDto());
        dtos.add(ExchangeObjectMother.generateLondonExchangeDto());
        dtos.add(ExchangeObjectMother.generateTokyoExchangeDto());
        dtos.add(ExchangeObjectMother.generateFrankfurtExchangeDto());
        Page<ExchangeDto> page = new PageImpl<>(dtos, pageable, dtos.size());
        return ResponseEntity.ok(page);
    }
}
