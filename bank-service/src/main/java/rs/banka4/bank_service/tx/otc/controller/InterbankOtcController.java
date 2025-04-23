package rs.banka4.bank_service.tx.otc.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.tx.data.OtcNegotiation;
import rs.banka4.bank_service.tx.data.OtcOffer;
import rs.banka4.bank_service.tx.data.PublicStock;
import rs.banka4.bank_service.tx.otc.service.InterbankOtcService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/interbank")
public class InterbankOtcController {

    private final InterbankOtcService interbankOtcService;

    @GetMapping("/public-stock")
    public ResponseEntity<List<PublicStock>> sendPublicStocks() {
        return ResponseEntity.ok(interbankOtcService.sendPublicStocks());
    }

    @PostMapping("/negotiations")
    public ResponseEntity<ForeignBankId> createOtc(@RequestBody OtcOffer offer) {
        return ResponseEntity.ok(interbankOtcService.createOtc(offer));
    }

    @PutMapping("/negotiations/{routingNumber}/{id}")
    public ResponseEntity<Void> createOtc(
        @RequestBody OtcOffer offer,
        @PathVariable long routingNumber,
        @PathVariable String id
    ) {
        interbankOtcService.updateOtc(offer, new ForeignBankId(routingNumber, id));
        return ResponseEntity.ok()
            .build();
    }

    @GetMapping("/negotiations/{routingNumber}/{id}")
    public ResponseEntity<OtcNegotiation> getNegotiation(
        @PathVariable long routingNumber,
        @PathVariable String id
    ) {
        return ResponseEntity.ok(
            interbankOtcService.getOtcNegotiation(new ForeignBankId(routingNumber, id))
        );
    }

    @DeleteMapping("/negotiations/{routingNumber}/{id}")
    public ResponseEntity<OtcNegotiation> closeNegotiation(
        @PathVariable long routingNumber,
        @PathVariable String id
    ) {
        interbankOtcService.closeNegotiation(new ForeignBankId(routingNumber, id));
        return ResponseEntity.ok()
            .build();
    }
}
