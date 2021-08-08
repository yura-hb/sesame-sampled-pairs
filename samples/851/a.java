import java.util.Map;

class PassiveExpiringMap&lt;K, V&gt; extends AbstractMapDecorator&lt;K, V&gt; implements Serializable {
    /**
     * All expired entries are removed from the map prior to returning the entry value.
     * {@inheritDoc}
     */
    @Override
    public V get(final Object key) {
	removeIfExpired(key, now());
	return super.get(key);
    }

    /** map used to manage expiration times for the actual map entries. */
    private final Map&lt;Object, Long&gt; expirationMap = new HashMap&lt;&gt;();

    /**
     * The current time in milliseconds.
     */
    private long now() {
	return System.currentTimeMillis();
    }

    /**
     * Removes the entry with the given key if the entry's expiration time is
     * less than &lt;code&gt;now&lt;/code&gt;. If the entry has a negative expiration time,
     * the entry is never removed.
     */
    private void removeIfExpired(final Object key, final long now) {
	final Long expirationTimeObject = expirationMap.get(key);
	if (isExpired(now, expirationTimeObject)) {
	    remove(key);
	}
    }

    /**
     * Determines if the given expiration time is less than &lt;code&gt;now&lt;/code&gt;.
     *
     * @param now the time in milliseconds used to compare against the
     *        expiration time.
     * @param expirationTimeObject the expiration time value retrieved from
     *        {@link #expirationMap}, can be null.
     * @return &lt;code&gt;true&lt;/code&gt; if &lt;code&gt;expirationTimeObject&lt;/code&gt; is &ge; 0
     *         and &lt;code&gt;expirationTimeObject&lt;/code&gt; &lt; &lt;code&gt;now&lt;/code&gt;.
     *         &lt;code&gt;false&lt;/code&gt; otherwise.
     */
    private boolean isExpired(final long now, final Long expirationTimeObject) {
	if (expirationTimeObject != null) {
	    final long expirationTime = expirationTimeObject.longValue();
	    return expirationTime &gt;= 0 && now &gt;= expirationTime;
	}
	return false;
    }

    /**
     * Normal {@link Map#remove(Object)} behavior with the addition of removing
     * any expiration entry as well.
     * {@inheritDoc}
     */
    @Override
    public V remove(final Object key) {
	expirationMap.remove(key);
	return super.remove(key);
    }

}

