package rs.banka4.user_service.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.user_service.domain.exchange.dtos.ExchangeRate;
import rs.banka4.user_service.domain.exchange.dtos.ExchangeRateDto;

/**
 * Service for handling exchange rate operations.
 *
 * <p>
 * This service fetches exchange rates from an external service and provides methods for currency
 * conversion and fee calculation.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private static final BigDecimal BANK_FEE = BigDecimal.valueOf(1.5);

    private final RestTemplate restTemplate;

    /**
     * Fetches the latest exchange rates from the external exchange rate service.
     *
     * @return an {@link ExchangeRateDto} containing the latest exchange rates.
     * @throws org.springframework.web.client.RestClientException if the external service is
     *         unavailable.
     */
    public ExchangeRateDto getExchangeRates() {
        String url = "http://exchange_office:8000/exchange-rate";
        ResponseEntity<ExchangeRateDto> response =
            restTemplate.exchange(url, HttpMethod.GET, null, ExchangeRateDto.class);
        return response.getBody();
    }

    /**
     * Converts an amount from one currency to another based on exchange rates.
     *
     * <p>
     * The conversion logic follows these rules:
     * </p>
     * <ul>
     * <li>If converting from RSD, the amount is divided by the buy rate of the target
     * currency.</li>
     * <li>If converting to RSD, the amount is multiplied by the sell rate of the source
     * currency.</li>
     * <li>If converting between two non-RSD currencies, the amount is first converted to RSD using
     * the sell rate, and then converted to the target currency using the buy rate.</li>
     * </ul>
     *
     * @param amount the amount to be converted.
     * @param from the source currency code.
     * @param to the target currency code.
     * @return the converted amount rounded to two decimal places.
     * @throws NullPointerException if exchange rates are not available for the given currencies.
     * @throws IllegalArgumentException if an invalid currency code is provided.
     */
    public BigDecimal convertCurrency(
        BigDecimal amount,
        CurrencyCode.Code from,
        CurrencyCode.Code to
    ) {
        ExchangeRateDto exchangeRateDto = getExchangeRates();
        Map<CurrencyCode.Code, ExchangeRate> exchangeRates = exchangeRateDto.exchanges();

        if (from.equals(CurrencyCode.Code.RSD)) {
            return amount.divide(
                exchangeRates.get(to)
                    .buy(),
                2,
                RoundingMode.HALF_UP
            );
        } else if (to.equals(CurrencyCode.Code.RSD)) {
            return amount.multiply(
                exchangeRates.get(from)
                    .sell()
            );
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calculates the transaction fee based on a given percentage.
     *
     * @param amount the transaction amount.
     * @return the calculated fee rounded to two decimal places.
     * @throws IllegalArgumentException if the fee percentage is negative.
     */
    public BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(BANK_FEE)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
