class Expirable&lt;V&gt; {
    /** Returns if the value has expired and is eligible for eviction. */
    public boolean hasExpired(long currentTimeMS) {
	return (currentTimeMS - expireTimeMS) &gt;= 0;
    }

    private volatile long expireTimeMS;

}

