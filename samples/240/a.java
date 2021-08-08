import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

interface CacheLoader&lt;K, V&gt; {
    /**
    * Asynchronously computes or retrieves a replacement value corresponding to an already-cached
    * {@code key}. If the replacement value is not found then the mapping will be removed if
    * {@code null} is computed. This method is called when an existing cache entry is refreshed by
    * {@link Caffeine#refreshAfterWrite}, or through a call to {@link LoadingCache#refresh}.
    * &lt;p&gt;
    * &lt;b&gt;Note:&lt;/b&gt; &lt;i&gt;all exceptions thrown by this method will be logged and then swallowed&lt;/i&gt;.
    *
    * @param key the non-null key whose value should be loaded
    * @param oldValue the non-null old value corresponding to {@code key}
    * @param executor the executor with which the entry is asynchronously loaded
    * @return a future containing the new value associated with {@code key}, or containing
    *         {@code null} if the mapping is to be removed
    */
    @Override
    @NonNull
    default CompletableFuture&lt;V&gt; asyncReload(@NonNull K key, @NonNull V oldValue, @NonNull Executor executor) {
	requireNonNull(key);
	requireNonNull(executor);
	return CompletableFuture.supplyAsync(() -&gt; {
	    try {
		return reload(key, oldValue);
	    } catch (RuntimeException e) {
		throw e;
	    } catch (Exception e) {
		throw new CompletionException(e);
	    }
	}, executor);
    }

    /**
    * Computes or retrieves a replacement value corresponding to an already-cached {@code key}. If
    * the replacement value is not found then the mapping will be removed if {@code null} is
    * returned. This method is called when an existing cache entry is refreshed by
    * {@link Caffeine#refreshAfterWrite}, or through a call to {@link LoadingCache#refresh}.
    * &lt;p&gt;
    * &lt;b&gt;Note:&lt;/b&gt; &lt;i&gt;all exceptions thrown by this method will be logged and then swallowed&lt;/i&gt;.
    *
    * @param key the non-null key whose value should be loaded
    * @param oldValue the non-null old value corresponding to {@code key}
    * @return the new value associated with {@code key}, or {@code null} if the mapping is to be
    *         removed
    * @throws Exception or Error, in which case the mapping is unchanged
    * @throws InterruptedException if this method is interrupted. {@code InterruptedException} is
    *         treated like any other {@code Exception} in all respects except that, when it is
    *         caught, the thread's interrupt status is set
    */
    @Nullable
    default V reload(@NonNull K key, @NonNull V oldValue) throws Exception {
	return load(key);
    }

    /**
    * Computes or retrieves the value corresponding to {@code key}.
    * &lt;p&gt;
    * &lt;b&gt;Warning:&lt;/b&gt; loading &lt;b&gt;must not&lt;/b&gt; attempt to update any mappings of this cache directly.
    *
    * @param key the non-null key whose value should be loaded
    * @return the value associated with {@code key} or {@code null} if not found
    * @throws Exception or Error, in which case the mapping is unchanged
    * @throws InterruptedException if this method is interrupted. {@code InterruptedException} is
    *         treated like any other {@code Exception} in all respects except that, when it is
    *         caught, the thread's interrupt status is set
    */
    @Nullable
    V load(@NonNull K key) throws Exception;

}

