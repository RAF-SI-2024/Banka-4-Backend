package rs.banka4.stock_service.service.abstraction;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.stock_service.domain.security.SecurityDto;
import rs.banka4.stock_service.domain.security.responses.SecurityOwnershipResponse;
import rs.banka4.stock_service.domain.security.responses.TaxSummaryResponse;
import rs.banka4.stock_service.domain.security.responses.TotalProfitResponse;


public interface SecuritiesService {
    // Object is StockDto, FutureDto or ForexDto
    ResponseEntity<Page<SecurityDto>> getSecurities(
        String securityType,
        String name,
        Pageable pageable
    );

    List<SecurityOwnershipResponse> getMySecurities(Authentication authentication);

    ResponseEntity<TotalProfitResponse> getTotalUnrealizedProfit(Authentication authentication);

    /**
     * Returns a tax summary for the current user, considering only stocks.
     * <p>
     * For each completed sell order (for stocks) in the current year, the capital gain is
     * calculated as: <br>
     * (sell price per unit - average purchase price per unit) * quantity sold. <br>
     * If the gain is positive, a 15% tax is computed. Gains from orders whose creation month is not
     * the current month are assumed to have been paid, while those in the current month are still
     * pending.
     * </p>
     *
     * @param authentication the current user's authentication token
     * @return a ResponseEntity containing the tax summary: - paidTaxThisYear: Total tax paid in
     *         previous months of the current year - unpaidTaxThisMonth: Tax accrued in the current
     *         month, yet to be collected - currency: hard-coded to "RSD"
     */
    ResponseEntity<TaxSummaryResponse> getTaxSummary(Authentication authentication);
}
