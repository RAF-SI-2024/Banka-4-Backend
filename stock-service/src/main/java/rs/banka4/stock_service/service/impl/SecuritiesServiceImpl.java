package rs.banka4.stock_service.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.stock_service.domain.security.SecurityDto;
import rs.banka4.stock_service.service.abstraction.SecuritiesService;

@Service
public class SecuritiesServiceImpl implements SecuritiesService {
    @Override
    public ResponseEntity<Page<SecurityDto>> getSecurities(
        String securityType,
        String name,
        Pageable pageable
    ) {
        return null;
    }
}
