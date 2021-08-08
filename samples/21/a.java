class CacheTesting {
    /**
    * Gets the {@link LocalCache} used by the given {@link Cache}, if any, or throws an
    * IllegalArgumentException if this is a Cache type that doesn't have a LocalCache.
    */
    static &lt;K, V&gt; LocalCache&lt;K, V&gt; toLocalCache(Cache&lt;K, V&gt; cache) {
	if (cache instanceof LocalLoadingCache) {
	    return ((LocalLoadingCache&lt;K, V&gt;) cache).localCache;
	}
	throw new IllegalArgumentException("Cache of type " + cache.getClass() + " doesn't have a LocalCache.");
    }

}

