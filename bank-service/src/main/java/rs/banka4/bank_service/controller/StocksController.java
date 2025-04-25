package rs.banka4.bank_service.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.banka4.bank_service.controller.docs.StocksApiDocumentation;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.assets.db.AssetOwnership;
import rs.banka4.bank_service.domain.assets.dtos.TransferDto;
import rs.banka4.bank_service.domain.trading.dtos.PublicStocksDto;
import rs.banka4.bank_service.service.abstraction.AssetOwnershipService;
import rs.banka4.bank_service.service.abstraction.ListingService;
import rs.banka4.bank_service.tx.otc.service.InterbankOtcService;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;

@RestController
@RequestMapping("/stock/stocks")
@RequiredArgsConstructor
public class StocksController implements StocksApiDocumentation {
    private final AssetOwnershipService assetOwnershipService;
    private final InterbankOtcService interbankOtcService;
    private final ListingService listingService;

    @Override
    @PutMapping("/transfer")
    public ResponseEntity<AssetOwnership> transferStocks(
        Authentication auth,
        @RequestBody @Valid TransferDto transferDto
    ) {
        final var ourAuth = (AuthenticatedBankUserAuthentication) auth;
        var userId =
            ourAuth.getPrincipal()
                .userId();
        return new ResponseEntity<>(
            assetOwnershipService.transferStock(
                userId,
                transferDto.stockId(),
                transferDto.amount(),
                transferDto.transferTo()
            ),
            HttpStatus.OK
        );
    }

    @GetMapping("{stockId}/latestPrice")
    public ResponseEntity<MonetaryAmount> getLatestStockPrice(@PathVariable UUID stockId) {
        return new ResponseEntity<>(listingService.getLatestPriceForStock(stockId), HttpStatus.OK);
    }

    @GetMapping("/public")
    public ResponseEntity<List<PublicStocksDto>> getPublicStocks(
        Authentication auth,
        Pageable pageable
    ) {
        final var ourAuth = (AuthenticatedBankUserAuthentication) auth;
        var token = ourAuth.getToken();
        return new ResponseEntity<>(
            interbankOtcService.getPublicStocks(pageable, token),
            HttpStatus.OK
        );
    }
}
