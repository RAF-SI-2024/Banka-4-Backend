package rs.banka4.bank_service.tx.otc.controller;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.exceptions.user.NotFound;
import rs.banka4.bank_service.service.abstraction.ForeignBankService;
import rs.banka4.bank_service.tx.data.OtcNegotiation;
import rs.banka4.bank_service.tx.data.OtcOffer;
import rs.banka4.bank_service.tx.data.PublicStock;
import rs.banka4.bank_service.tx.data.UserInformation;
import rs.banka4.bank_service.tx.otc.service.InterbankOtcService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/interbank")
public class InterbankOtcController {

    private final InterbankOtcService interbankOtcService;
    private final ForeignBankService foreignBankService;

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

    @GetMapping("/negotiations/{routingNumber}/{id}/accept")
    public ResponseEntity<Void> acceptNegotiation(
        @PathVariable long routingNumber,
        @PathVariable String id
    ) {
        interbankOtcService.acceptNegotiation(new ForeignBankId(routingNumber, id));
        return ResponseEntity.ok()
            .build();
    }

    @GetMapping("/user/{routingNumber}/{id}")
    public ResponseEntity<UserInformation> getUserInfo(
        @PathVariable long routingNumber,
        @PathVariable String id
    ) throws IOException {
        return ResponseEntity.ok(
            foreignBankService.getUserInfoFor(new ForeignBankId(routingNumber, id))
                .orElseThrow(NotFound::new)
        );
    }
}
