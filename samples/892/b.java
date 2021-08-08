class XMLSecurityManager {
    /**
     * Check if there's no limit defined by the Security Manager
     * @param limit
     * @return
     */
    public boolean isNoLimit(int limit) {
	return limit == NO_LIMIT;
    }

    private static final int NO_LIMIT = 0;

}

