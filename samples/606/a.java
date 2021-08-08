import java.util.concurrent.atomic.AtomicReference;

abstract class AtomicSafeInitializer&lt;T&gt; implements ConcurrentInitializer&lt;T&gt; {
    /**
     * Get (and initialize, if not initialized yet) the required object
     *
     * @return lazily initialized object
     * @throws ConcurrentException if the initialization of the object causes an
     * exception
     */
    @Override
    public final T get() throws ConcurrentException {
	T result;

	while ((result = reference.get()) == null) {
	    if (factory.compareAndSet(null, this)) {
		reference.set(initialize());
	    }
	}

	return result;
    }

    /** Holds the reference to the managed object. */
    private final AtomicReference&lt;T&gt; reference = new AtomicReference&lt;&gt;();
    /** A guard which ensures that initialize() is called only once. */
    private final AtomicReference&lt;AtomicSafeInitializer&lt;T&gt;&gt; factory = new AtomicReference&lt;&gt;();

    /**
     * Creates and initializes the object managed by this
     * {@code AtomicInitializer}. This method is called by {@link #get()} when
     * the managed object is not available yet. An implementation can focus on
     * the creation of the object. No synchronization is needed, as this is
     * already handled by {@code get()}. This method is guaranteed to be called
     * only once.
     *
     * @return the managed data object
     * @throws ConcurrentException if an error occurs during object creation
     */
    protected abstract T initialize() throws ConcurrentException;

}

