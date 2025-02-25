package rs.banka4.user_service.models;

import org.springframework.security.core.GrantedAuthority;

public enum Privilege implements GrantedAuthority {

    ADMIN,
    FILTER,
    SEARCH,
    TRADE_STOCKS,
    VIEW_STOCKS,
    CONTRACTS,
    NEW_INSURANCES;

    @Override
    public String getAuthority() {
        return name();
    }

    public long bit() {
        return 1L << this.ordinal();
    }
}
