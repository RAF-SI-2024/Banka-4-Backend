package rs.banka4.bank_service.service.impl;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rs.banka4.bank_service.domain.assets.db.AssetOwnership;
import rs.banka4.bank_service.domain.security.stock.db.Stock;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.trading.db.OtcMapper;
import rs.banka4.bank_service.domain.trading.db.OtcRequest;
import rs.banka4.bank_service.domain.trading.db.RequestStatus;
import rs.banka4.bank_service.domain.trading.db.dtos.OtcRequestCreateDto;
import rs.banka4.bank_service.domain.trading.db.dtos.OtcRequestUpdateDto;
import rs.banka4.bank_service.exceptions.*;
import rs.banka4.bank_service.repositories.AssetOwnershipRepository;
import rs.banka4.bank_service.repositories.OtcRequestRepository;
import rs.banka4.bank_service.repositories.StockRepository;
import rs.banka4.bank_service.service.abstraction.AccountService;
import rs.banka4.bank_service.service.abstraction.OtcRequestService;
import rs.banka4.bank_service.service.abstraction.TaxService;
import rs.banka4.bank_service.service.abstraction.TradingService;
import rs.banka4.bank_service.tx.otc.mapper.InterbankOtcMapper;
import rs.banka4.bank_service.tx.otc.service.InterbankOtcService;

@Service
@RequiredArgsConstructor
public class OtcRequestServiceImp implements OtcRequestService {
    private final OtcRequestRepository otcRequestRepository;
    private final OtcMapper otcMapper;
    private final AssetOwnershipRepository assetOwnershipRepository;
    private final TradingService tradingService;
    private final InterbankOtcService interbankOtcService;
    private final StockRepository stockRepository;
    private final TaxService taxService;
    private final AccountService accountService;

    @Override
    public Page<OtcRequest> getMyRequests(Pageable pageable, UUID myId) {
        return otcRequestRepository.findActiveRequestsByUser(myId.toString(), pageable);
    }

    @Override
    public Page<OtcRequest> getMyRequestsUnread(Pageable pageable, UUID myId) {
        return otcRequestRepository.findActiveUnreadRequestsByUser(myId.toString(), pageable);
    }

    @Override
    public void rejectOtc(ForeignBankId requestId) {
        var otc =
            otcRequestRepository.findById(requestId)
                .orElseThrow(() -> new OtcNotFoundException(requestId));
        if (
            !otc.getStatus()
                .equals(RequestStatus.ACTIVE)
        ) throw new RequestFailed();
        otc.setStatus(RequestStatus.REJECTED);
        otcRequestRepository.save(otc);
        // send update to other bank
        if (routingNumber(otc) != -1) {
            interbankOtcService.sendCloseNegotiation(requestId, routingNumber(otc));
        }
    }

    @Override
    public void updateOtc(
        OtcRequestUpdateDto otcRequestUpdateDto,
        ForeignBankId id,
        UUID modifiedBy
    ) {
        var otc =
            otcRequestRepository.findById(id)
                .orElseThrow(() -> new OtcNotFoundException(id));
        var modBy = ForeignBankId.our(modifiedBy);
        otcMapper.update(otc, otcRequestUpdateDto, modBy);
        otcRequestRepository.save(otc);
        // send update to other bank
        if (routingNumber(otc) != -1) {
            interbankOtcService.sendUpdateOtc(
                InterbankOtcMapper.INSTANCE.toOtcOffer(otc),
                id,
                routingNumber(otc)
            );
        }
    }

    @Override
    public void createOtc(OtcRequestCreateDto otcRequestCreateDto, UUID idMy) {
        AssetOwnership assetOwner;
        if (
            otcRequestCreateDto.userId()
                .routingNumber()
                == ForeignBankId.OUR_ROUTING_NUMBER
        ) {
            assetOwner =
                assetOwnershipRepository.findByMyId(
                    UUID.fromString(
                        otcRequestCreateDto.userId()
                            .id()
                    ),
                    otcRequestCreateDto.assetId()
                )
                    .orElseThrow(
                        () -> new StockOwnershipNotFound(
                            UUID.fromString(
                                otcRequestCreateDto.userId()
                                    .id()
                            ),
                            otcRequestCreateDto.assetId()
                        )
                    );
            if (assetOwner.getPublicAmount() < otcRequestCreateDto.amount())
                throw new RequestFailed();
        }

        var me = ForeignBankId.our(idMy);
        var madeFor = otcRequestCreateDto.userId();
        Stock stock =
            stockRepository.findById(otcRequestCreateDto.assetId())
                .orElseThrow(AssetNotFound::new);
        var newOtc =
            otcMapper.toOtcRequest(
                otcRequestCreateDto,
                me,
                madeFor,
                me,
                RequestStatus.ACTIVE,
                stock
            );
        otcRequestRepository.save(newOtc);
        if (madeFor.routingNumber() != ForeignBankId.OUR_ROUTING_NUMBER)
            interbankOtcService.sendCreateOtc(InterbankOtcMapper.INSTANCE.toOtcOffer(newOtc));
    }

    @Override
    public void acceptOtc(ForeignBankId requestId, UUID userId) {
        Optional<OtcRequest> otcRequest = otcRequestRepository.findById(requestId);
        if (otcRequest.isEmpty()) throw new OtcNotFoundException(requestId);
        OtcRequest otc = otcRequest.get();
        ForeignBankId ourUserId = ForeignBankId.our(userId);
        if (
            otc.getMadeBy()
                .equals(ourUserId)
                || otc.getMadeFor()
                    .equals(ourUserId)
        ) {
            if (
                !otc.getModifiedBy()
                    .equals(ourUserId)
            ) {


                // send update to other bank
                if (routingNumber(otc) != -1) {
                    interbankOtcService.sendAcceptNegotiation(requestId, routingNumber(otc));
                    if (
                        otc.getMadeFor()
                            .routingNumber()
                            == ForeignBankId.OUR_ROUTING_NUMBER
                    ) {
                        var accNum =
                            accountService.getRequiredAccount(
                                UUID.fromString(
                                    otc.getMadeFor()
                                        .id()
                                ),
                                otc.getPremium()
                                    .getCurrency(),
                                BigDecimal.ZERO
                            );
                        if (accNum.isPresent()) {
                            var acc =
                                accountService.getAccountByAccountNumber(
                                    accNum.get()
                                        .accountNumber()
                                );
                            taxService.addTaxAmountToDB(otc.getPremium(), acc);
                        }
                    }
                } else {
                    tradingService.sendPremiumAndGetOption(otc);
                    var accNum =
                        accountService.getRequiredAccount(
                            UUID.fromString(
                                otc.getMadeFor()
                                    .id()
                            ),
                            otc.getPremium()
                                .getCurrency(),
                            BigDecimal.ZERO
                        );
                    if (accNum.isPresent()) {
                        var acc =
                            accountService.getAccountByAccountNumber(
                                accNum.get()
                                    .accountNumber()
                            );
                        taxService.addTaxAmountToDB(otc.getPremium(), acc);
                    }
                }
            } else {
                throw new CantAcceptThisOffer("Other side has to accept the offer", userId);
            }
        } else {
            throw new CantAcceptThisOffer("You are not in this offer", userId);
        }
    }

    private long routingNumber(OtcRequest otcRequest) {
        if (
            otcRequest.getMadeBy()
                .routingNumber()
                != ForeignBankId.OUR_ROUTING_NUMBER
        )
            return otcRequest.getMadeBy()
                .routingNumber();
        if (
            otcRequest.getMadeFor()
                .routingNumber()
                != ForeignBankId.OUR_ROUTING_NUMBER
        )
            return otcRequest.getMadeFor()
                .routingNumber();
        return -1;
    }
}
