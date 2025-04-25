package rs.banka4.bank_service.service.abstraction;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import rs.banka4.bank_service.domain.account.db.Account;
import rs.banka4.bank_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.bank_service.domain.options.db.Option;
import rs.banka4.bank_service.domain.orders.db.Order;
import rs.banka4.bank_service.domain.taxes.db.dto.TaxableUserDto;

public interface TaxService {
    Page<TaxableUserDto> getTaxSummary(String firstName, String lastName, PageRequest of);

    void taxUser(UUID userId);

    void taxMonthly();

    /**
     * Calculates realized profit for a completed sell order and records the corresponding tax debt.
     * <p>
     * If the order is not a SELL or the asset is not a Stock, this method does nothing. Otherwise,
     * it computes realized profit via
     * {@link ProfitCalculationService#calculateRealizedProfitForSell}, then delegates to
     * {@link #addTaxAmountToDB} to persist or update the tax debt.
     *
     * @param order the order for which to calculate and record tax; must be non-null
     */
    void addTaxForOrderToDB(Order order);

    /**
     * Calculates realized profit for a completed otc trade and records the corresponding tax debt.
     * <p>
     * It computes realized profit via
     * {@link ProfitCalculationService#calculateOptionProfit(Option)}, then delegates to
     * {@link #addTaxAmountToDB} to persist or update the tax debt.
     *
     * @param option the option for which to calculate and record tax; must be non-null
     * @param amount
     */
    void addTaxForOtcToDB(Option option, Account account, int amount);

    /**
     * Adds a tax amount to the database for the given account.
     * <p>
     * If {@code taxAmount} is zero or negative, this method does nothing. If no existing
     * {@code UserTaxDebts} record exists for the account, a new one is created. Otherwise, the
     * existing debt is incremented by {@code taxAmount}. The currency of {@code taxAmount} must
     * match the account’s currency.
     *
     * @param profitAmount the amount of profit for which the function will find tax to add; must be
     *        non-null and in the same currency as {@code account}
     * @param account the account to which the tax debt belongs; must be non-null
     * @throws IllegalArgumentException if {@code taxAmount.getCurrency()} differs from
     *         {@code account.getCurrency()}
     */
    void addTaxAmountToDB(MonetaryAmount profitAmount, Account account);
}
