package rs.banka4.stock_service.domain.options.db;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.apache.commons.math3.distribution.NormalDistribution;

public class BlackHolesOptionPricing {
    private static final NormalDistribution N = new NormalDistribution();

    /**
     * Calculates the price of an option using the Black-Scholes model.
     *
     * @param S the current stock price
     * @param K the strike price
     * @param T the time to expiration (in years)
     * @param r the risk-free interest rate (annualized)
     * @param sigma the implied volatility of the underlying asset (annualized)
     * @param type the type of the option (CALL or PUT)
     * @return the calculated option price
     */
    public static double calculateOptionPrice(
        double S,
        double K,
        double T,
        double r,
        double sigma,
        OptionType type
    ) {
        double d1 = (Math.log(S / K) + (r + 0.5 * sigma * sigma) * T) / (sigma * Math.sqrt(T));
        double d2 = d1 - sigma * Math.sqrt(T);

        if (type == OptionType.CALL) {
            return S * N.cumulativeProbability(d1)
                - K * Math.exp(-r * T) * N.cumulativeProbability(d2);
        } else {
            return K * Math.exp(-r * T) * N.cumulativeProbability(-d2)
                - S * N.cumulativeProbability(-d1);
        }
    }

    /**
     * Calculates the price of an option based on the given Option object and current stock price.
     *
     * <p>
     * This method extracts necessary parameters from the provided Option object, including strike
     * price, settlement date, implied volatility, and option type. It calculates the time to
     * expiration and uses a fixed risk-free rate to compute the option price using the
     * Black-Scholes model.
     *
     * @param option the Option object containing option parameters except premium which is null (or
     *        ignored if not)
     * @param currentStockPrice the current price of the underlying stock (taken from an active
     *        listing)
     * @return the calculated option price, or 0.0 if the option has already expired
     */
    public static double calculateOptionPriceFromOption(Option option, double currentStockPrice) {
        double riskFreeRate = 0.02;
        double S = currentStockPrice;
        double K =
            option.getStrikePrice()
                .getAmount()
                .doubleValue();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        double T =
            ChronoUnit.SECONDS.between(now, option.getSettlementDate()) / (365.25 * 24 * 60 * 60);

        if (T <= 0) {
            return 0.0;
        }

        double r = riskFreeRate;
        double sigma = option.getImpliedVolatility();
        OptionType type = option.getOptionType();

        return calculateOptionPrice(S, K, T, r, sigma, type);
    }
}
