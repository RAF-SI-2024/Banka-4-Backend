package rs.banka4.stock_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.stock_service.controller.docs.StocksApiDocumentation;
import rs.banka4.stock_service.domain.assets.db.AssetOwnership;
import rs.banka4.stock_service.domain.assets.dtos.TransferDto;
import rs.banka4.stock_service.service.abstraction.AssetOwnershipService;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StocksController implements StocksApiDocumentation {
    private final AssetOwnershipService assetOwnershipService;

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
}
