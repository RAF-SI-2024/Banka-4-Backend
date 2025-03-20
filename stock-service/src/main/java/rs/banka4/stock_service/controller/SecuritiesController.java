package rs.banka4.stock_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rs.banka4.stock_service.controller.docs.SecuritiesApiDocumentation;
import rs.banka4.stock_service.domain.security.SecurityDto;
import rs.banka4.stock_service.service.abstraction.SecuritiesService;

@RestController
@RequestMapping("/securities")
@RequiredArgsConstructor
public class SecuritiesController implements SecuritiesApiDocumentation {

    private final SecuritiesService securityService;

    @Override
    @GetMapping
    public ResponseEntity<Page<SecurityDto>> getSecurities(
        @RequestParam(required = false) String securityType,
        @RequestParam(required = false) String name,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return securityService.getSecurities(securityType, name, PageRequest.of(page, size));
    }
}
