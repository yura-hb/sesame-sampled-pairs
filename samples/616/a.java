import com.github.benmanes.caffeine.cache.testing.CacheSpec.Implementation;

class CacheGenerator {
    /** Creates a new cache based on the context's configuration. */
    public static &lt;K, V&gt; Cache&lt;K, V&gt; newCache(CacheContext context) {
	switch (context.implementation()) {
	case Caffeine:
	    return CaffeineCacheFromContext.newCaffeineCache(context);
	case Guava:
	    return GuavaCacheFromContext.newGuavaCache(context);
	}
	throw new IllegalStateException();
    }

}

