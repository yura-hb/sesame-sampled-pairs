import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

class UnboundedLocalCache&lt;K, V&gt; implements LocalCache&lt;K, V&gt; {
    /**
    * A {@link Map#compute(Object, BiFunction)} that does not directly record any cache statistics.
    *
    * @param key key with which the specified value is to be associated
    * @param remappingFunction the function to compute a value
    * @return the new value associated with the specified key, or null if none
    */
    V remap(K key, BiFunction&lt;? super K, ? super V, ? extends V&gt; remappingFunction) {
	// ensures that the removal notification is processed after the removal has completed
	@SuppressWarnings({ "unchecked", "rawtypes" })
	V[] oldValue = (V[]) new Object[1];
	RemovalCause[] cause = new RemovalCause[1];
	V nv = data.compute(key, (K k, V value) -&gt; {
	    V newValue = remappingFunction.apply(k, value);
	    if ((value == null) && (newValue == null)) {
		return null;
	    }

	    cause[0] = (newValue == null) ? RemovalCause.EXPLICIT : RemovalCause.REPLACED;
	    if (hasRemovalListener() && (value != null) && (newValue != value)) {
		oldValue[0] = value;
	    }

	    return newValue;
	});
	if (oldValue[0] != null) {
	    notifyRemoval(key, oldValue[0], cause[0]);
	}
	return nv;
    }

    final ConcurrentHashMap&lt;K, V&gt; data;
    @Nullable
    final RemovalListener&lt;K, V&gt; removalListener;
    final Executor executor;

    @Override
    public boolean hasRemovalListener() {
	return (removalListener != null);
    }

    @Override
    public void notifyRemoval(@Nullable K key, @Nullable V value, RemovalCause cause) {
	requireNonNull(removalListener(), "Notification should be guarded with a check");
	executor.execute(() -&gt; removalListener().onRemoval(key, value, cause));
    }

    @Override
    @SuppressWarnings("NullAway")
    public RemovalListener&lt;K, V&gt; removalListener() {
	return removalListener;
    }

}

