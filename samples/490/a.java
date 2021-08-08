class StaticBucketMap&lt;K, V&gt; extends AbstractIterableMap&lt;K, V&gt; {
    /**
     * Checks if the map contains the specified key.
     *
     * @param key  the key to check
     * @return true if found
     */
    @Override
    public boolean containsKey(final Object key) {
	final int hash = getHash(key);

	synchronized (locks[hash]) {
	    Node&lt;K, V&gt; n = buckets[hash];

	    while (n != null) {
		if (n.key == key || (n.key != null && n.key.equals(key))) {
		    return true;
		}

		n = n.next;
	    }
	}
	return false;
    }

    /** The matching array of locks */
    private final Lock[] locks;
    /** The array of buckets, where the actual data is held */
    private final Node&lt;K, V&gt;[] buckets;

    /**
     * Determine the exact hash entry for the key.  The hash algorithm
     * is rather simplistic, but it does the job:
     *
     * &lt;pre&gt;
     *   He = |Hk mod n|
     * &lt;/pre&gt;
     *
     * &lt;p&gt;
     *   He is the entry's hashCode, Hk is the key's hashCode, and n is
     *   the number of buckets.
     * &lt;/p&gt;
     */
    private int getHash(final Object key) {
	if (key == null) {
	    return 0;
	}
	int hash = key.hashCode();
	hash += ~(hash &lt;&lt; 15);
	hash ^= (hash &gt;&gt;&gt; 10);
	hash += (hash &lt;&lt; 3);
	hash ^= (hash &gt;&gt;&gt; 6);
	hash += ~(hash &lt;&lt; 11);
	hash ^= (hash &gt;&gt;&gt; 16);
	hash %= buckets.length;
	return (hash &lt; 0) ? hash * -1 : hash;
    }

}

