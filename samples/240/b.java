import com.google.common.util.concurrent.Futures;

abstract class CacheLoader&lt;K, V&gt; {
    /**
    * Computes or retrieves a replacement value corresponding to an already-cached {@code key}. This
    * method is called when an existing cache entry is refreshed by {@link
    * CacheBuilder#refreshAfterWrite}, or through a call to {@link LoadingCache#refresh}.
    *
    * &lt;p&gt;This implementation synchronously delegates to {@link #load}. It is recommended that it be
    * overridden with an asynchronous implementation when using {@link
    * CacheBuilder#refreshAfterWrite}.
    *
    * &lt;p&gt;&lt;b&gt;Note:&lt;/b&gt; &lt;i&gt;all exceptions thrown by this method will be logged and then swallowed&lt;/i&gt;.
    *
    * @param key the non-null key whose value should be loaded
    * @param oldValue the non-null old value corresponding to {@code key}
    * @return the future new value associated with {@code key}; &lt;b&gt;must not be null, must not return
    *     null&lt;/b&gt;
    * @throws Exception if unable to reload the result
    * @throws InterruptedException if this method is interrupted. {@code InterruptedException} is
    *     treated like any other {@code Exception} in all respects except that, when it is caught,
    *     the thread's interrupt status is set
    * @since 11.0
    */
    @GwtIncompatible // Futures
    public ListenableFuture&lt;V&gt; reload(K key, V oldValue) throws Exception {
	checkNotNull(key);
	checkNotNull(oldValue);
	return Futures.immediateFuture(load(key));
    }

    /**
    * Computes or retrieves the value corresponding to {@code key}.
    *
    * @param key the non-null key whose value should be loaded
    * @return the value associated with {@code key}; &lt;b&gt;must not be null&lt;/b&gt;
    * @throws Exception if unable to load the result
    * @throws InterruptedException if this method is interrupted. {@code InterruptedException} is
    *     treated like any other {@code Exception} in all respects except that, when it is caught,
    *     the thread's interrupt status is set
    */
    public abstract V load(K key) throws Exception;

}

