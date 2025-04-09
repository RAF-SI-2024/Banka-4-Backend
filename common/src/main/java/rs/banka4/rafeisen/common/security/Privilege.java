package rs.banka4.rafeisen.common.security;

public enum Privilege {
    /* *NEVER* reorder elements in this enum - their order is significant. */
    /**
     * Privilege for administrators. Users with this privilege have full access to all system
     * functionalities.
     */
    ADMIN,

    /**
     * Privilege for supervisors. Users with this privilege can oversee and manage agents.
     */
    SUPERVISOR,

    /**
     * Privilege for agents. Users with this privilege can perform trade operations and other
     * agent-specific tasks.
     */
    AGENT,

    /**
     * Privilege for trade operations. Users with this privilege can execute trade-related tasks.
     */
    TRADE;

    public long bit() {
        return 1L << this.ordinal();
    }
}
