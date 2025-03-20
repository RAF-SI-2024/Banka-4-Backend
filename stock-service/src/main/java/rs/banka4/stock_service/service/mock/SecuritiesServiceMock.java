package rs.banka4.stock_service.service.mock;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.stock_service.domain.security.SecurityDto;
import rs.banka4.stock_service.service.abstraction.SecuritiesService;
import rs.banka4.stock_service.service.mock.generators.SecuritiesObjectMother;

@Service
@Primary
public class SecuritiesServiceMock implements SecuritiesService {
    @Override
    public ResponseEntity<Page<SecurityDto>> getSecurities(
        String securityType,
        String name,
        Pageable pageable
    ) {
        List<SecurityDto> dtos = new ArrayList<>();
        dtos.add(SecuritiesObjectMother.generateForexPairSecurityDto());
        dtos.add(SecuritiesObjectMother.generateFutureSecurityDto());
        dtos.add(SecuritiesObjectMother.generateStockSecurityDto());
        Page<SecurityDto> page = new PageImpl<>(dtos, pageable, 3);
        return ResponseEntity.ok(page);
    }
}
