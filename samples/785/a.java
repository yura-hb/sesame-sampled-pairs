import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Advance;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.CacheExpiry;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Compute;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Expire;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Implementation;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Population;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.ReferenceType;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Writer;

class CacheGenerator {
    /** Returns a lazy stream so that the test case is GC-able after use. */
    public Stream&lt;Entry&lt;CacheContext, Cache&lt;Integer, Integer&gt;&gt;&gt; generate() {
	return combinations().stream().map(this::newCacheContext).filter(this::isCompatible).map(context -&gt; {
	    Cache&lt;Integer, Integer&gt; cache = newCache(context);
	    populate(context, cache);
	    return Maps.immutableEntry(context, cache);
	});
    }

    private final Options options;
    private final CacheSpec cacheSpec;
    private final boolean isAsyncOnly;
    private final boolean isLoadingOnly;

    /** Returns the Cartesian set of the possible cache configurations. */
    @SuppressWarnings("unchecked")
    private Set&lt;List&lt;Object&gt;&gt; combinations() {
	Set&lt;Boolean&gt; asyncLoading = ImmutableSet.of(true, false);
	Set&lt;Stats&gt; statistics = filterTypes(options.stats(), cacheSpec.stats());
	Set&lt;ReferenceType&gt; keys = filterTypes(options.keys(), cacheSpec.keys());
	Set&lt;ReferenceType&gt; values = filterTypes(options.values(), cacheSpec.values());
	Set&lt;Compute&gt; computations = filterTypes(options.compute(), cacheSpec.compute());
	Set&lt;Implementation&gt; implementations = filterTypes(options.implementation(), cacheSpec.implementation());

	if (isAsyncOnly) {
	    values = values.contains(ReferenceType.STRONG) ? ImmutableSet.of(ReferenceType.STRONG) : ImmutableSet.of();
	    computations = Sets.filter(computations, Compute.ASYNC::equals);
	}
	if (isAsyncOnly || computations.equals(ImmutableSet.of(Compute.ASYNC))) {
	    implementations = implementations.contains(Implementation.Caffeine)
		    ? ImmutableSet.of(Implementation.Caffeine)
		    : ImmutableSet.of();
	}
	if (computations.equals(ImmutableSet.of(Compute.SYNC))) {
	    asyncLoading = ImmutableSet.of(false);
	}

	if (computations.isEmpty() || implementations.isEmpty() || keys.isEmpty() || values.isEmpty()) {
	    return ImmutableSet.of();
	}
	return Sets.cartesianProduct(ImmutableSet.copyOf(cacheSpec.initialCapacity()), ImmutableSet.copyOf(statistics),
		ImmutableSet.copyOf(cacheSpec.weigher()), ImmutableSet.copyOf(cacheSpec.maximumSize()),
		ImmutableSet.copyOf(cacheSpec.expiry()), ImmutableSet.copyOf(cacheSpec.expireAfterAccess()),
		ImmutableSet.copyOf(cacheSpec.expireAfterWrite()), ImmutableSet.copyOf(cacheSpec.refreshAfterWrite()),
		ImmutableSet.copyOf(cacheSpec.advanceOnPopulation()), ImmutableSet.copyOf(keys),
		ImmutableSet.copyOf(values), ImmutableSet.copyOf(cacheSpec.executor()),
		ImmutableSet.copyOf(cacheSpec.removalListener()), ImmutableSet.copyOf(cacheSpec.population()),
		ImmutableSet.of(true, isLoadingOnly), ImmutableSet.copyOf(asyncLoading),
		ImmutableSet.copyOf(computations), ImmutableSet.copyOf(cacheSpec.loader()),
		ImmutableSet.copyOf(cacheSpec.writer()), ImmutableSet.copyOf(implementations));
    }

    /** Returns a new cache context based on the combination. */
    private CacheContext newCacheContext(List&lt;Object&gt; combination) {
	int index = 0;
	return new CacheContext((InitialCapacity) combination.get(index++), (Stats) combination.get(index++),
		(CacheWeigher) combination.get(index++), (Maximum) combination.get(index++),
		(CacheExpiry) combination.get(index++), (Expire) combination.get(index++),
		(Expire) combination.get(index++), (Expire) combination.get(index++),
		(Advance) combination.get(index++), (ReferenceType) combination.get(index++),
		(ReferenceType) combination.get(index++), (CacheExecutor) combination.get(index++),
		(Listener) combination.get(index++), (Population) combination.get(index++),
		(Boolean) combination.get(index++), (Boolean) combination.get(index++),
		(Compute) combination.get(index++), (Loader) combination.get(index++),
		(Writer) combination.get(index++), (Implementation) combination.get(index++), cacheSpec);
    }

    /** Returns if the context is a viable configuration. */
    private boolean isCompatible(CacheContext context) {
	boolean asyncIncompatible = context.isAsync()
		&& ((context.implementation() != Implementation.Caffeine) || !context.isStrongValues());
	boolean asyncLoaderIncompatible = context.isAsyncLoading() && (!context.isAsync() || !context.isLoading());
	boolean refreshIncompatible = context.refreshes() && !context.isLoading();
	boolean weigherIncompatible = context.isUnbounded() && context.isWeighted();
	boolean referenceIncompatible = cacheSpec.requiresWeakOrSoft() && context.isStrongKeys()
		&& context.isStrongValues();
	boolean expiryIncompatible = (context.expiryType() != CacheExpiry.DISABLED)
		&& ((context.implementation() != Implementation.Caffeine)
			|| (context.expireAfterAccess() != Expire.DISABLED)
			|| (context.expireAfterWrite() != Expire.DISABLED));
	boolean expirationIncompatible = (cacheSpec.mustExpireWithAnyOf().length &gt; 0)
		&& !Arrays.stream(cacheSpec.mustExpireWithAnyOf()).anyMatch(context::expires);

	boolean skip = asyncIncompatible || asyncLoaderIncompatible || refreshIncompatible || weigherIncompatible
		|| expiryIncompatible || expirationIncompatible || referenceIncompatible;
	return !skip;
    }

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

    /** Fills the cache up to the population size. */
    @SuppressWarnings({ "deprecation", "unchecked", "BoxedPrimitiveConstructor" })
    private void populate(CacheContext context, Cache&lt;Integer, Integer&gt; cache) {
	if (context.population.size() == 0) {
	    return;
	}

	// Integer caches the object identity semantics of autoboxing for values between
	// -128 and 127 (inclusive) as required by JLS
	int base = 1000;

	int maximum = (int) Math.min(context.maximumSize(), context.population.size());
	int first = base + (int) Math.min(1, context.population.size());
	int last = base + maximum;
	int middle = Math.max(first, base + ((last - first) / 2));

	context.disableRejectingCacheWriter();
	for (int i = 1; i &lt;= maximum; i++) {
	    // Reference caching (weak, soft) require unique instances for identity comparison
	    Integer key = new Integer(base + i);
	    Integer value = new Integer(-key);

	    if (key == first) {
		context.firstKey = key;
	    }
	    if (key == middle) {
		context.middleKey = key;
	    }
	    if (key == last) {
		context.lastKey = key;
	    }
	    cache.put(key, value);
	    context.original.put(key, value);
	    context.ticker().advance(context.advance.timeNanos(), TimeUnit.NANOSECONDS);
	}
	context.enableRejectingCacheWriter();
	if (context.writer() == Writer.MOCKITO) {
	    reset(context.cacheWriter());
	}
    }

    /** Returns the set of options filtered if a specific type is specified. */
    private static &lt;T&gt; Set&lt;T&gt; filterTypes(Optional&lt;T&gt; type, T[] options) {
	if (type.isPresent()) {
	    return type.filter(Arrays.asList(options)::contains).isPresent() ? ImmutableSet.of(type.get())
		    : ImmutableSet.of();
	}
	return ImmutableSet.copyOf(Arrays.asList(options));
    }

}

