class TerminatingThreadLocal&lt;T&gt; extends ThreadLocal&lt;T&gt; {
    /**
     * Invokes the TerminatingThreadLocal's {@link #threadTerminated()} method
     * on all instances registered in current thread.
     */
    public static void threadTerminated() {
	for (TerminatingThreadLocal&lt;?&gt; ttl : REGISTRY.get()) {
	    ttl._threadTerminated();
	}
    }

    /**
     * a per-thread registry of TerminatingThreadLocal(s) that have been registered
     * but later not unregistered in a particular thread.
     */
    public static final ThreadLocal&lt;Collection&lt;TerminatingThreadLocal&lt;?&gt;&gt;&gt; REGISTRY = new ThreadLocal&lt;&gt;() {
	@Override
	protected Collection&lt;TerminatingThreadLocal&lt;?&gt;&gt; initialValue() {
	    return Collections.newSetFromMap(new IdentityHashMap&lt;&gt;(4));
	}
    };

    private void _threadTerminated() {
	threadTerminated(get());
    }

    /**
     * Invoked by a thread when terminating and this thread-local has an associated
     * value for the terminating thread (even if that value is null), so that any
     * native resources maintained by the value can be released.
     *
     * @param value current thread's value of this thread-local variable
     *              (may be null but only if null value was explicitly initialized)
     */
    protected void threadTerminated(T value) {
    }

}

