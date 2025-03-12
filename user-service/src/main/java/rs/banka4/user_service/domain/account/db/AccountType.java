package rs.banka4.user_service.domain.account.db;

import java.util.Arrays;
import java.util.List;

public enum AccountType {
    // Personal account types
    STANDARD,
    SAVINGS,
    RETIREMENT,
    YOUTH,
    STUDENT,
    UNEMPLOYED,

    // Business account types
    DOO,
    AD,
    FOUNDATION;

    // List of personal account types
    public static final List<AccountType> PERSONAL_ACCOUNTS =
        Arrays.asList(STANDARD, SAVINGS, RETIREMENT, YOUTH, STUDENT, UNEMPLOYED);

    // List of business account types
    public static final List<AccountType> BUSINESS_ACCOUNTS = Arrays.asList(DOO, AD, FOUNDATION);

    // Check if account type is personal
    public boolean isPersonal() {
        return PERSONAL_ACCOUNTS.contains(this);
    }

    // Check if account type is business
    public boolean isBusiness() {
        return BUSINESS_ACCOUNTS.contains(this);
    }
}
