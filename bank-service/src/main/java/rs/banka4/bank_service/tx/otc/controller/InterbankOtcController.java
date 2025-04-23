package rs.banka4.bank_service.tx.otc.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.banka4.bank_service.tx.data.PublicStock;
import rs.banka4.bank_service.tx.otc.service.InterbankOtcService;

@RestController
@RequiredArgsConstructor
public class InterbankOtcController {

    private final InterbankOtcService interbankOtcService;

    @GetMapping("/public-stock")
    public ResponseEntity<List<PublicStock>> sendPublicStocks() {
        return ResponseEntity.ok(interbankOtcService.sendPublicStocks());
    }
}
