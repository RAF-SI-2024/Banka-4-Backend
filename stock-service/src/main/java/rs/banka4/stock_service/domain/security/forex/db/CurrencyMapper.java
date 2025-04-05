package rs.banka4.stock_service.domain.security.forex.db;

import java.util.HashMap;
import java.util.Map;

public class CurrencyMapper {
    private static final Map<String, CurrencyCode> currencyMap = new HashMap<>();

    static {
        currencyMap.put("Euro", CurrencyCode.EUR);
        currencyMap.put("United States Dollar", CurrencyCode.USD);
        currencyMap.put("USD", CurrencyCode.USD); // handle both formats
        currencyMap.put("Australian Dollar", CurrencyCode.AUD);
        currencyMap.put("Canadian Dollar", CurrencyCode.CAD);
        // Other currencies not in the enum will not be mapped
    }

    public static CurrencyCode mapToCurrencyCode(String currencyName) {
        return currencyMap.getOrDefault(currencyName, null);
    }
}
