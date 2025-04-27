package rs.banka4.bank_service.service.abstraction;

import java.util.UUID;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.options.db.Asset;
import rs.banka4.bank_service.domain.options.db.Option;
import rs.banka4.bank_service.domain.orders.db.Order;
import rs.banka4.bank_service.domain.user.employee.db.Employee;

public interface ProfitCalculationService {
    /**
     * Calculates total profit (unrealized) for the given user and asset. In case of option pls give
     * current price of stock that option has.
     */
    MonetaryAmount calculateProfit(
        UUID userId,
        Asset asset,
        MonetaryAmount currentPrice,
        int totalAmount
    );

    /**
     * Calculates profit (unrealized/realized) for the given option.
     */
    MonetaryAmount calculateOptionProfit(Option option, int totalAmount);

    /**
     * Calculate realized profit (P&L) for exactly one sell order, using FIFO matching against prior
     * buys (including partial fills), and accounting for both buy- and sell-side fees.
     *
     * @param sellOrder the SELL {@link Order} for which we want realized profit
     * @return a {@link MonetaryAmount} in the sell order’s currency
     */
    MonetaryAmount calculateRealizedProfitForSell(Order sellOrder);

    /**
     * Calculate realized profit (P&L) for exactly one actuary, using FIFO matching against prior
     * buys (including partial fills), and accounting for both buy- and sell-side fees.
     *
     * @param actuary the {@link Employee} for which we want realized profit
     * @return a {@link MonetaryAmount} in the RSD currency
     */
    MonetaryAmount calculateRealizedProfitForActuary(Employee actuary);
}
