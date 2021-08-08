abstract class BackgroundInitializer&lt;T&gt; implements ConcurrentInitializer&lt;T&gt; {
    /**
     * Returns the {@code Future} object that was created when {@link #start()}
     * was called. Therefore this method can only be called after {@code
     * start()}.
     *
     * @return the {@code Future} object wrapped by this initializer
     * @throws IllegalStateException if {@link #start()} has not been called
     */
    public synchronized Future&lt;T&gt; getFuture() {
	if (future == null) {
	    throw new IllegalStateException("start() must be called first!");
	}

	return future;
    }

    /** Stores the handle to the background task. */
    private Future&lt;T&gt; future;

}

