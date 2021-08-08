import com.google.common.base.Throwables;
import com.google.common.cache.LoadingCache;

class SubscriberRegistry {
    /**
    * Flattens a class's type hierarchy into a set of {@code Class} objects including all
    * superclasses (transitively) and all interfaces implemented by these superclasses.
    */
    @VisibleForTesting
    static ImmutableSet&lt;Class&lt;?&gt;&gt; flattenHierarchy(Class&lt;?&gt; concreteClass) {
	try {
	    return flattenHierarchyCache.getUnchecked(concreteClass);
	} catch (UncheckedExecutionException e) {
	    throw Throwables.propagate(e.getCause());
	}
    }

    /** Global cache of classes to their flattened hierarchy of supertypes. */
    private static final LoadingCache&lt;Class&lt;?&gt;, ImmutableSet&lt;Class&lt;?&gt;&gt;&gt; flattenHierarchyCache = CacheBuilder
	    .newBuilder().weakKeys().build(new CacheLoader&lt;Class&lt;?&gt;, ImmutableSet&lt;Class&lt;?&gt;&gt;&gt;() {
		// &lt;Class&lt;?&gt;&gt; is actually needed to compile
		@SuppressWarnings("RedundantTypeArguments")
		@Override
		public ImmutableSet&lt;Class&lt;?&gt;&gt; load(Class&lt;?&gt; concreteClass) {
		    return ImmutableSet.&lt;Class&lt;?&gt;&gt;copyOf(TypeToken.of(concreteClass).getTypes().rawTypes());
		}
	    });

}

