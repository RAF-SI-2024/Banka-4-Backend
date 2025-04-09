package rs.banka4.stock_service.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rs.banka4.stock_service.domain.listing.db.Listing;
import rs.banka4.stock_service.domain.listing.db.ListingDailyPriceInfo;
import rs.banka4.stock_service.domain.listing.dtos.*;
import rs.banka4.stock_service.domain.listing.dtos.ListingFilterDto;
import rs.banka4.stock_service.domain.listing.dtos.ListingInfoDto;
import rs.banka4.stock_service.domain.listing.dtos.specific.ForexPairDto;
import rs.banka4.stock_service.domain.listing.dtos.specific.FutureDto;
import rs.banka4.stock_service.domain.listing.dtos.specific.StockDto;
import rs.banka4.stock_service.domain.listing.mapper.ListingDailyPriceInfoMapper;
import rs.banka4.stock_service.domain.listing.mapper.ListingMapper;
import rs.banka4.stock_service.domain.listing.specificaion.ListingSpecification;
import rs.banka4.stock_service.domain.options.db.Option;
import rs.banka4.stock_service.domain.options.db.OptionType;
import rs.banka4.stock_service.domain.security.Security;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.future.db.Future;
import rs.banka4.stock_service.domain.security.stock.db.Stock;
import rs.banka4.stock_service.exceptions.ListingNotFoundException;
import rs.banka4.stock_service.repositories.ListingDailyPriceInfoRepository;
import rs.banka4.stock_service.repositories.ListingRepository;
import rs.banka4.stock_service.repositories.OptionsRepository;
import rs.banka4.stock_service.repositories.OrderRepository;
import rs.banka4.stock_service.service.abstraction.ListingService;

@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {
    private final ListingRepository listingRepository;
    private final OrderRepository orderRepository;
    private final ListingDailyPriceInfoRepository listingDailyPriceInfoRepository;
    private final OptionsRepository optionsRepository;

    @Override
    public int getVolumeOfAsset(UUID securityId) {
        var now = OffsetDateTime.now();
        var start = now.truncatedTo(ChronoUnit.DAYS);
        var end = start.plusDays(1);
        return orderRepository.findCountOfMadeOrdersToday(securityId, start, end);
    }

    /**
     * returns null if there is no yesterday listing value returns change by calculating from last
     * price from yesterday
     */
    @Override
    public BigDecimal calculateChange(UUID securityId, BigDecimal currentPrice) {
        Optional<ListingDailyPriceInfo> yesterdayListing =
            listingDailyPriceInfoRepository.getYesterdayListingDailyPriceInfo(
                securityId,
                Limit.of(1)
            );
        return yesterdayListing.map(
            listingDailyPriceInfo -> currentPrice.subtract(listingDailyPriceInfo.getLastPrice())
        )
            .orElse(null);
    }

    @Override
    public Page<ListingInfoDto> getListings(
        ListingFilterDto filter,
        Pageable pageable,
        boolean isClient
    ) {
        var req =
            listingRepository.findAll(
                ListingSpecification.getSpecification(filter, isClient),
                pageable
            );
        return req.map((listing) -> {
            var vol =
                getVolumeOfAsset(
                    listing.getSecurity()
                        .getId()
                );
            var change =
                calculateChange(
                    listing.getSecurity()
                        .getId(),
                    listing.getAsk()
                );
            return ListingMapper.INSTANCE.toInfoDto(listing, vol, change);
        });
    }

    @Override
    public List<PriceChangeDto> getPriceChanges(UUID securityId) {
        List<ListingDailyPriceInfo> infos =
            listingDailyPriceInfoRepository.findAllBySecurityId(securityId);
        return infos.stream()
            .map(ListingDailyPriceInfoMapper.INSTANCE::toPriceChangeDto)
            .toList();
    }

    @Override
    public ListingDetailsDto getListingDetails(UUID securityId) {
        Optional<Listing> listing = listingRepository.getLatestListing(securityId, Limit.of(1));
        if (listing.isEmpty()) {
            throw new ListingNotFoundException(securityId);
        } else {
            Security security =
                listing.get()
                    .getSecurity();

            if (security instanceof Stock) {
                StockDto stockDto = ListingMapper.INSTANCE.toStockDto((Stock) security);
                setupDto(stockDto, listing.get());
                return stockDto;
            } else if (security instanceof Future) {
                FutureDto futureDto = ListingMapper.INSTANCE.toFutureDto((Future) security);
                setupDto(futureDto, listing.get());
                return futureDto;
            } else {
                ForexPairDto forexPairDto =
                    ListingMapper.INSTANCE.toForexPairDto((ForexPair) security);
                setupDto(forexPairDto, listing.get());
                return forexPairDto;
            }
        }
    }

    /**
     * @return Specific format of two options in one Dto that should fit one row in table on
     *         frontend
     * @param stockId Id of a specific stock that we look Options for
     * @param settlementDate Date on which Options should expire function gets all options for a
     *        listed stock that are expiring on a settlement date, and filters them to two maps
     *        based on Option type where key is a strike price. After that its just filing dto in
     *        right order where left side is for CALLs and right side is for PUTs
     */

    @Override
    public List<OptionDto> getOptionsWithSettlementDateForStock(
        UUID stockId,
        OffsetDateTime settlementDate
    ) {
        var start = settlementDate.truncatedTo(ChronoUnit.DAYS);
        var end = start.plusDays(1);
        List<Option> options =
            optionsRepository.findAllOptionsForStockWithSettlementDate(stockId, start, end);
        Map<BigDecimal, Option> callsOptions = new HashMap<>();
        Map<BigDecimal, Option> putsOptions = new HashMap<>();
        SortedSet<BigDecimal> strikes = new TreeSet<>();
        for (var x : options) {
            strikes.add(
                x.getStrikePrice()
                    .getAmount()
            );
            if (x.getOptionType() == OptionType.CALL)
                callsOptions.put(
                    x.getStrikePrice()
                        .getAmount(),
                    x
                );
            else
                putsOptions.put(
                    x.getStrikePrice()
                        .getAmount(),
                    x
                );
        }

        List<OptionDto> optionDtos = new ArrayList<>();

        for (var strike : strikes) {
            Option call = callsOptions.get(strike);
            Option puts = putsOptions.get(strike);
            optionDtos.add(
                new OptionDto(
                    call.getId(),
                    new BigDecimal("0"),
                    new BigDecimal("0"),
                    call.getImpliedVolatility(),
                    getVolumeOfAsset(call.getId()),
                    call.getOpenInterest(),
                    call.getPremium(),
                    strike,
                    puts.getId(),
                    new BigDecimal("0"),
                    new BigDecimal("0"),
                    puts.getImpliedVolatility(),
                    getVolumeOfAsset(puts.getId()),
                    puts.getOpenInterest(),
                    puts.getPremium()
                )
            );
        }
        return optionDtos;
    }

    private void setupDto(ListingDetailsDto dto, Listing listing) {
        dto.setName(
            listing.getSecurity()
                .getName()
        );
        dto.setPrice(listing.getAsk());
        dto.setTicker(
            listing.getSecurity()
                .getTicker()
        );
        dto.setVolume(
            getVolumeOfAsset(
                listing.getSecurity()
                    .getId()
            )
        );
        dto.setChange(
            calculateChange(
                listing.getSecurity()
                    .getId(),
                listing.getAsk()
            )
        );
    }

    public Optional<Listing> findActiveListingByAsset(UUID assetId) {
        return listingRepository.findBySecurityIdAndActiveTrue(assetId);
    }

}
