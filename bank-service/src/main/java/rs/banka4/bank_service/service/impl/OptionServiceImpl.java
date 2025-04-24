package rs.banka4.bank_service.service.impl;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.banka4.bank_service.domain.options.db.Option;
import rs.banka4.bank_service.domain.options.db.OptionType;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.exceptions.*;
import rs.banka4.bank_service.repositories.AssetOwnershipRepository;
import rs.banka4.bank_service.repositories.OptionsRepository;
import rs.banka4.bank_service.repositories.OtcRequestRepository;
import rs.banka4.bank_service.service.abstraction.OptionService;
import rs.banka4.bank_service.service.abstraction.OtcRequestService;
import rs.banka4.bank_service.service.abstraction.TradingService;
import rs.banka4.rafeisen.common.dto.AccountNumberDto;

@Service
@RequiredArgsConstructor
public class OptionServiceImpl implements OptionService {
    private final OptionsRepository optionsRepository;
    private final TradingService tradingService;
    private final AssetOwnershipRepository assetOwnershipRepository;
    private final OtcRequestRepository otcRequestRepository;
    private final OtcRequestService otcRequestService;

    @Override
    public void buyOption(UUID optionId, UUID userId, String accountNumber, int amount) {
        // TODO check if account number is user's account and if he has enough money on it
        var option = optionsRepository.findById(optionId);
        if (
            option.isEmpty()
                || !option.get()
                    .isActive()
        ) {
            throw new OptionNotFound();
        }
        tradingService.buyOption(option.get(), userId, accountNumber, amount);
    }

    @Override
    public void useOption(UUID optionId, UUID userId, String accountNumber) {
        var ownership = assetOwnershipRepository.findByMyId(userId, optionId);
        if (
            ownership.isEmpty()
                || !(ownership.get()
                    .getId()
                    .getAsset() instanceof Option option)
        ) {
            throw new OptionOwnershipNotFound(optionId, userId);
        }
        if (
            !option.getSettlementDate()
                .isAfter(OffsetDateTime.now())
        ) {
            throw new OptionExpired();
        }
        if (option.getOptionType() == OptionType.PUT) {
            var stockOwnership =
                assetOwnershipRepository.findByMyId(
                    userId,
                    option.getStock()
                        .getId()
                );
            if (stockOwnership.isEmpty()) {
                throw new StockOwnershipNotFound(
                    option.getStock()
                        .getId(),
                    userId
                );
            }
            if (
                ownership.get()
                    .getPrivateAmount()
                    <= stockOwnership.get()
                        .getPrivateAmount()
            ) {
                tradingService.usePutOption(
                    option,
                    userId,
                    accountNumber,
                    ownership.get()
                        .getPrivateAmount()
                );
            } else {
                throw new NotEnoughStock();
            }
        } else {
            var otcRequest = otcRequestRepository.findByOptionId(optionId);
            if (otcRequest.isEmpty()) {
                // exchange option
                tradingService.useCallOptionFromExchange(
                    option,
                    userId,
                    accountNumber,
                    ownership.get()
                        .getPrivateAmount()
                );
            } else {
                // otc option
                AccountNumberDto sellerAccount =
                    otcRequestService.getRequiredAccount(
                        UUID.fromString(
                            otcRequest.get()
                                .getMadeFor()
                                .userId()
                        ),
                        otcRequest.get()
                            .getPricePerStock()
                            .getCurrency(),
                        null
                    );
                tradingService.useCallOptionFromOtc(
                    option,
                    ForeignBankId.our(userId),
                    otcRequest.get()
                        .getMadeFor(),
                    accountNumber,
                    sellerAccount.accountNumber(),
                    otcRequest.get()
                        .getAmount()
                );
            }
        }
    }
}
