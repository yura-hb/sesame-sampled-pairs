import java.lang.invoke.VarHandle;
import java.util.function.BiConsumer;

class CompletableFuture&lt;T&gt; implements Future&lt;T&gt;, CompletionStage&lt;T&gt; {
    /**
     * Exceptionally completes this CompletableFuture with
     * a {@link TimeoutException} if not otherwise completed
     * before the given timeout.
     *
     * @param timeout how long to wait before completing exceptionally
     *        with a TimeoutException, in units of {@code unit}
     * @param unit a {@code TimeUnit} determining how to interpret the
     *        {@code timeout} parameter
     * @return this CompletableFuture
     * @since 9
     */
    public CompletableFuture&lt;T&gt; orTimeout(long timeout, TimeUnit unit) {
	if (unit == null)
	    throw new NullPointerException();
	if (result == null)
	    whenComplete(new Canceller(Delayer.delay(new Timeout(this), timeout, unit)));
	return this;
    }

    volatile Object result;
    private static final VarHandle NEXT;
    static final int SYNC = 0;
    volatile Completion stack;
    private static final VarHandle STACK;
    private static final VarHandle RESULT;

    public CompletableFuture&lt;T&gt; whenComplete(BiConsumer&lt;? super T, ? super Throwable&gt; action) {
	return uniWhenCompleteStage(null, action);
    }

    private CompletableFuture&lt;T&gt; uniWhenCompleteStage(Executor e, BiConsumer&lt;? super T, ? super Throwable&gt; f) {
	if (f == null)
	    throw new NullPointerException();
	CompletableFuture&lt;T&gt; d = newIncompleteFuture();
	Object r;
	if ((r = result) == null)
	    unipush(new UniWhenComplete&lt;T&gt;(e, d, this, f));
	else if (e == null)
	    d.uniWhenComplete(r, f, null);
	else {
	    try {
		e.execute(new UniWhenComplete&lt;T&gt;(null, d, this, f));
	    } catch (Throwable ex) {
		d.result = encodeThrowable(ex);
	    }
	}
	return d;
    }

    /**
     * Returns a new incomplete CompletableFuture of the type to be
     * returned by a CompletionStage method. Subclasses should
     * normally override this method to return an instance of the same
     * class as this CompletableFuture. The default implementation
     * returns an instance of class CompletableFuture.
     *
     * @param &lt;U&gt; the type of the value
     * @return a new CompletableFuture
     * @since 9
     */
    public &lt;U&gt; CompletableFuture&lt;U&gt; newIncompleteFuture() {
	return new CompletableFuture&lt;U&gt;();
    }

    /**
     * Pushes the given completion unless it completes while trying.
     * Caller should first check that result is null.
     */
    final void unipush(Completion c) {
	if (c != null) {
	    while (!tryPushStack(c)) {
		if (result != null) {
		    NEXT.set(c, null);
		    break;
		}
	    }
	    if (result != null)
		c.tryFire(SYNC);
	}
    }

    final boolean uniWhenComplete(Object r, BiConsumer&lt;? super T, ? super Throwable&gt; f, UniWhenComplete&lt;T&gt; c) {
	T t;
	Throwable x = null;
	if (result == null) {
	    try {
		if (c != null && !c.claim())
		    return false;
		if (r instanceof AltResult) {
		    x = ((AltResult) r).ex;
		    t = null;
		} else {
		    @SuppressWarnings("unchecked")
		    T tr = (T) r;
		    t = tr;
		}
		f.accept(t, x);
		if (x == null) {
		    internalComplete(r);
		    return true;
		}
	    } catch (Throwable ex) {
		if (x == null)
		    x = ex;
		else if (x != ex)
		    x.addSuppressed(ex);
	    }
	    completeThrowable(x, r);
	}
	return true;
    }

    /**
     * Returns the encoding of the given (non-null) exception as a
     * wrapped CompletionException unless it is one already.
     */
    static AltResult encodeThrowable(Throwable x) {
	return new AltResult((x instanceof CompletionException) ? x : new CompletionException(x));
    }

    /**
     * Creates a new incomplete CompletableFuture.
     */
    public CompletableFuture() {
    }

    /** Returns true if successfully pushed c onto stack. */
    final boolean tryPushStack(Completion c) {
	Completion h = stack;
	NEXT.set(c, h); // CAS piggyback
	return STACK.compareAndSet(this, h, c);
    }

    final boolean internalComplete(Object r) { // CAS from null to r
	return RESULT.compareAndSet(this, null, r);
    }

    /**
     * Completes with the given (non-null) exceptional result as a
     * wrapped CompletionException unless it is one already, unless
     * already completed.  May complete with the given Object r
     * (which must have been the result of a source future) if it is
     * equivalent, i.e. if this is a simple propagation of an
     * existing CompletionException.
     */
    final boolean completeThrowable(Throwable x, Object r) {
	return RESULT.compareAndSet(this, null, encodeThrowable(x, r));
    }

    /**
     * Returns the encoding of the given (non-null) exception as a
     * wrapped CompletionException unless it is one already.  May
     * return the given Object r (which must have been the result of a
     * source future) if it is equivalent, i.e. if this is a simple
     * relay of an existing CompletionException.
     */
    static Object encodeThrowable(Throwable x, Object r) {
	if (!(x instanceof CompletionException))
	    x = new CompletionException(x);
	else if (r instanceof AltResult && x == ((AltResult) r).ex)
	    return r;
	return new AltResult(x);
    }

    class Timeout implements Runnable {
	volatile Object result;
	private static final VarHandle NEXT;
	static final int SYNC = 0;
	volatile Completion stack;
	private static final VarHandle STACK;
	private static final VarHandle RESULT;

	Timeout(CompletableFuture&lt;?&gt; f) {
	    this.f = f;
	}

    }

    class Delayer {
	volatile Object result;
	private static final VarHandle NEXT;
	static final int SYNC = 0;
	volatile Completion stack;
	private static final VarHandle STACK;
	private static final VarHandle RESULT;

	static ScheduledFuture&lt;?&gt; delay(Runnable command, long delay, TimeUnit unit) {
	    return delayer.schedule(command, delay, unit);
	}

    }

    class Canceller implements BiConsumer&lt;Object, Throwable&gt; {
	volatile Object result;
	private static final VarHandle NEXT;
	static final int SYNC = 0;
	volatile Completion stack;
	private static final VarHandle STACK;
	private static final VarHandle RESULT;

	Canceller(Future&lt;?&gt; f) {
	    this.f = f;
	}

    }

    class UniWhenComplete&lt;T&gt; extends UniCompletion&lt;T, T&gt; {
	volatile Object result;
	private static final VarHandle NEXT;
	static final int SYNC = 0;
	volatile Completion stack;
	private static final VarHandle STACK;
	private static final VarHandle RESULT;

	UniWhenComplete(Executor executor, CompletableFuture&lt;T&gt; dep, CompletableFuture&lt;T&gt; src,
		BiConsumer&lt;? super T, ? super Throwable&gt; fn) {
	    super(executor, dep, src);
	    this.fn = fn;
	}

    }

    abstract class UniCompletion&lt;T, V&gt; extends Completion {
	volatile Object result;
	private static final VarHandle NEXT;
	static final int SYNC = 0;
	volatile Completion stack;
	private static final VarHandle STACK;
	private static final VarHandle RESULT;

	UniCompletion(Executor executor, CompletableFuture&lt;V&gt; dep, CompletableFuture&lt;T&gt; src) {
	    this.executor = executor;
	    this.dep = dep;
	    this.src = src;
	}

	/**
	 * Returns true if action can be run. Call only when known to
	 * be triggerable. Uses FJ tag bit to ensure that only one
	 * thread claims ownership.  If async, starts as task -- a
	 * later call to tryFire will run action.
	 */
	final boolean claim() {
	    Executor e = executor;
	    if (compareAndSetForkJoinTaskTag((short) 0, (short) 1)) {
		if (e == null)
		    return true;
		executor = null; // disable
		e.execute(this);
	    }
	    return false;
	}

    }

    abstract class Completion extends ForkJoinTask&lt;Void&gt; implements Runnable, AsynchronousCompletionTask {
	volatile Object result;
	private static final VarHandle NEXT;
	static final int SYNC = 0;
	volatile Completion stack;
	private static final VarHandle STACK;
	private static final VarHandle RESULT;

	/**
	 * Performs completion action if triggered, returning a
	 * dependent that may need propagation, if one exists.
	 *
	 * @param mode SYNC, ASYNC, or NESTED
	 */
	abstract CompletableFuture&lt;?&gt; tryFire(int mode);

    }

    class AltResult {
	volatile Object result;
	private static final VarHandle NEXT;
	static final int SYNC = 0;
	volatile Completion stack;
	private static final VarHandle STACK;
	private static final VarHandle RESULT;

	AltResult(Throwable x) {
	    this.ex = x;
	}

    }

}

