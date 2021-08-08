class StaticBucketMap&lt;K, V&gt; extends AbstractIterableMap&lt;K, V&gt; {
    /**
     * Gets the current size of the map.
     * The value is computed fresh each time the method is called.
     *
     * @return the current size
     */
    @Override
    public int size() {
	int cnt = 0;

	for (int i = 0; i &lt; buckets.length; i++) {
	    synchronized (locks[i]) {
		cnt += locks[i].size;
	    }
	}
	return cnt;
    }

    /** The array of buckets, where the actual data is held */
    private final Node&lt;K, V&gt;[] buckets;
    /** The matching array of locks */
    private final Lock[] locks;

}

