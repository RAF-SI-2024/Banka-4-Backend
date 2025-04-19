package rs.banka4.bank_service.generator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.assets.db.AssetOwnership;
import rs.banka4.bank_service.domain.assets.db.AssetOwnershipId;
import rs.banka4.bank_service.domain.security.stock.db.Stock;
import rs.banka4.bank_service.domain.trading.db.ForeignBankId;
import rs.banka4.bank_service.domain.trading.db.OtcRequest;
import rs.banka4.bank_service.domain.trading.db.RequestStatus;
import rs.banka4.bank_service.domain.user.User;
import rs.banka4.bank_service.repositories.*;
import rs.banka4.bank_service.utils.AssetGenerator;
import rs.banka4.bank_service.utils.ExchangeGenerator;
import rs.banka4.bank_service.utils.ListingGenerator;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.testlib.utils.JwtPlaceholders;

public class OtcRequestGen {

    public static OtcRequest createDummyOtcRequest(
        AssetRepository assetRepository,
        SecurityRepository securityRepository,
        OtcRequestRepository otcRequestRepository,
        ListingRepository listingRepo,
        ListingDailyPriceInfoRepository listingHistoryRepo,
        ExchangeRepository exchangeRepo
    ) {
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);
        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(ex1.get(), ber1, listingRepo, listingHistoryRepo);

        var momo = new MonetaryAmount();
        momo.setAmount(BigDecimal.ONE);
        momo.setCurrency(CurrencyCode.AUD);
        OtcRequest req = new OtcRequest();
        req.setStock((Stock) ex1.get());
        req.setMadeBy(ForeignBankId.our(UUID.randomUUID()));
        req.setMadeFor(ForeignBankId.our(JwtPlaceholders.CLIENT_ID));
        req.setModifiedBy(ForeignBankId.our(UUID.randomUUID()));
        req.setStatus(RequestStatus.ACTIVE);
        req.setPremium(momo);
        req.setPricePerStock(momo);
        req.setSettlementDate(LocalDate.parse("2025-04-11"));
        req.setPricePerStock(momo);
        req.setAmount(1);

        otcRequestRepository.save(req);
        return req;
    }

    public static OtcRequest createDummyOtcRequestNotMe(
        AssetRepository assetRepository,
        SecurityRepository securityRepository,
        OtcRequestRepository otcRequestRepository,
        ListingRepository listingRepo,
        ListingDailyPriceInfoRepository listingHistoryRepo,
        ExchangeRepository exchangeRepo
    ) {
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(ex1.get(), ber1, listingRepo, listingHistoryRepo);
        var momo = new MonetaryAmount();
        momo.setAmount(BigDecimal.ONE);
        momo.setCurrency(CurrencyCode.AUD);
        OtcRequest req = new OtcRequest();
        req.setStock((Stock) ex1.get());
        req.setMadeBy(ForeignBankId.our(UUID.randomUUID()));
        req.setMadeFor(ForeignBankId.our(UUID.randomUUID()));
        req.setModifiedBy(ForeignBankId.our(UUID.randomUUID()));
        req.setStatus(RequestStatus.ACTIVE);
        req.setPremium(momo);
        req.setPricePerStock(momo);
        req.setSettlementDate(LocalDate.parse("2025-04-11"));
        req.setPricePerStock(momo);
        req.setAmount(1);

        otcRequestRepository.save(req);
        return req;
    }

    public static OtcRequest createDummyOtcRequestMeUnread(
        AssetRepository assetRepository,
        SecurityRepository securityRepository,
        OtcRequestRepository otcRequestRepository,
        ListingRepository listingRepo,
        ListingDailyPriceInfoRepository listingHistoryRepo,
        ExchangeRepository exchangeRepo
    ) {
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(ex1.get(), ber1, listingRepo, listingHistoryRepo);
        var momo = new MonetaryAmount();
        momo.setAmount(BigDecimal.ONE);
        momo.setCurrency(CurrencyCode.AUD);
        OtcRequest req = new OtcRequest();
        req.setStock((Stock) ex1.get());
        req.setMadeBy(ForeignBankId.our(UUID.randomUUID()));
        req.setMadeFor(ForeignBankId.our(JwtPlaceholders.CLIENT_ID));
        req.setModifiedBy(ForeignBankId.our(UUID.randomUUID()));
        req.setStatus(RequestStatus.ACTIVE);
        req.setPremium(momo);
        req.setPricePerStock(momo);
        req.setSettlementDate(LocalDate.parse("2025-04-11"));
        req.setPricePerStock(momo);
        req.setAmount(1);

        otcRequestRepository.save(req);
        return req;
    }

    public static OtcRequest createDummyOtcRequestMeRead(
        AssetRepository assetRepository,
        SecurityRepository securityRepository,
        OtcRequestRepository otcRequestRepository,
        ListingRepository listingRepo,
        ListingDailyPriceInfoRepository listingHistoryRepo,
        ExchangeRepository exchangeRepo
    ) {
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(ex1.get(), ber1, listingRepo, listingHistoryRepo);
        var momo = new MonetaryAmount();
        momo.setAmount(BigDecimal.ONE);
        momo.setCurrency(CurrencyCode.AUD);
        OtcRequest req = new OtcRequest();
        req.setStock((Stock) ex1.get());
        req.setMadeBy(ForeignBankId.our(UUID.randomUUID()));
        req.setMadeFor(ForeignBankId.our(JwtPlaceholders.CLIENT_ID));
        req.setModifiedBy(ForeignBankId.our(JwtPlaceholders.CLIENT_ID));
        req.setStatus(RequestStatus.ACTIVE);
        req.setPremium(momo);
        req.setPricePerStock(momo);
        req.setSettlementDate(LocalDate.parse("2025-04-11"));
        req.setPricePerStock(momo);
        req.setAmount(1);

        otcRequestRepository.save(req);
        return req;
    }

    public static OtcRequest createDummyOtcRequestMeFinished(
        AssetRepository assetRepository,
        SecurityRepository securityRepository,
        OtcRequestRepository otcRequestRepository,
        int requestAmount,
        ListingRepository listingRepo,
        ListingDailyPriceInfoRepository listingHistoryRepo,
        ExchangeRepository exchangeRepo
    ) {
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(ex1.get(), ber1, listingRepo, listingHistoryRepo);
        var momo = new MonetaryAmount();
        momo.setAmount(BigDecimal.TEN);
        momo.setCurrency(CurrencyCode.AUD);
        OtcRequest req = new OtcRequest();
        req.setStock((Stock) ex1.get());
        req.setMadeBy(ForeignBankId.our(UUID.randomUUID()));
        req.setMadeFor(ForeignBankId.our(JwtPlaceholders.CLIENT_ID));
        req.setModifiedBy(ForeignBankId.our(JwtPlaceholders.CLIENT_ID));
        req.setStatus(RequestStatus.FINISHED);
        req.setPremium(momo);
        req.setPricePerStock(momo);
        req.setSettlementDate(LocalDate.parse("2025-04-12"));
        req.setPricePerStock(momo);
        req.setAmount(requestAmount);
        req.setOptionId(AssetGenerator.OPTION_EX1_PUT_UUID);

        otcRequestRepository.saveAndFlush(req);
        return req;
    }

    public static void setupDummyAssetOwnership(
        User owner,
        int totalAvailable,
        AssetRepository assetRepository,
        AssetOwnershipRepository assetOwnershipRepository,
        SecurityRepository securityRepository,
        ListingRepository listingRepo,
        ListingDailyPriceInfoRepository listingHistoryRepo,
        ExchangeRepository exchangeRepo
    ) {
        AssetGenerator.makeExampleAssets()
            .forEach(assetRepository::saveAndFlush);

        final var ber1 = ExchangeGenerator.makeBer1();
        exchangeRepo.save(ber1);
        var ex1 = securityRepository.findById(AssetGenerator.STOCK_EX1_UUID);
        ListingGenerator.makeExampleListings(ex1.get(), ber1, listingRepo, listingHistoryRepo);
        AssetOwnershipId id = new AssetOwnershipId(owner, ex1.get());
        AssetOwnership assetOwnership = new AssetOwnership();
        assetOwnership.setId(id);
        assetOwnership.setPublicAmount(totalAvailable / 2);
        assetOwnership.setReservedAmount(totalAvailable / 2);
        assetOwnershipRepository.save(assetOwnership);
    }

    public static AssetOwnership createDummyAssetOwnership(
        User owner,
        int reservedAmount,
        int publicAmount,
        AssetRepository assetRepository,
        AssetOwnershipRepository assetOwnershipRepository
    ) {
//        AssetGenerator.makeExampleAssets()
//            .forEach(assetRepository::saveAndFlush);

        var ex1 = assetRepository.findById(AssetGenerator.OPTION_EX1_PUT_UUID);
        AssetOwnershipId ownershipId = new AssetOwnershipId();
        ownershipId.setUser(owner);
        ownershipId.setAsset(ex1.get());

        AssetOwnership assetOwnership = new AssetOwnership();
        assetOwnership.setId(ownershipId);
        assetOwnership.setReservedAmount(reservedAmount);
        assetOwnership.setPublicAmount(publicAmount);

        return assetOwnershipRepository.saveAndFlush(assetOwnership);
    }
}
