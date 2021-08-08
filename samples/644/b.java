import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

class GcFinalization {
    /**
    * Waits until the given weak reference is cleared, invoking the garbage collector as necessary to
    * try to ensure that this will happen.
    *
    * &lt;p&gt;This is a convenience method, equivalent to:
    *
    * &lt;pre&gt;{@code
    * awaitDone(new FinalizationPredicate() {
    *   public boolean isDone() {
    *     return ref.get() == null;
    *   }
    * });
    * }&lt;/pre&gt;
    *
    * @throws RuntimeException if timed out or interrupted while waiting
    */
    public static void awaitClear(final WeakReference&lt;?&gt; ref) {
	awaitDone(new FinalizationPredicate() {
	    public boolean isDone() {
		return ref.get() == null;
	    }
	});
    }

    /**
    * Waits until the given predicate returns true, invoking the garbage collector as necessary to
    * try to ensure that this will happen.
    *
    * @throws RuntimeException if timed out or interrupted while waiting
    */
    public static void awaitDone(FinalizationPredicate predicate) {
	if (predicate.isDone()) {
	    return;
	}
	final long timeoutSeconds = timeoutSeconds();
	final long deadline = System.nanoTime() + SECONDS.toNanos(timeoutSeconds);
	do {
	    System.runFinalization();
	    if (predicate.isDone()) {
		return;
	    }
	    CountDownLatch done = new CountDownLatch(1);
	    createUnreachableLatchFinalizer(done);
	    await(done);
	    if (predicate.isDone()) {
		return;
	    }
	} while (System.nanoTime() - deadline &lt; 0);
	throw formatRuntimeException("Predicate did not become true within %d second timeout", timeoutSeconds);
    }

    /**
    * 10 seconds ought to be long enough for any object to be GC'ed and finalized. Unless we have a
    * gigantic heap, in which case we scale by heap size.
    */
    private static long timeoutSeconds() {
	// This class can make no hard guarantees.  The methods in this class are inherently flaky, but
	// we try hard to make them robust in practice.  We could additionally try to add in a system
	// load timeout multiplier.  Or we could try to use a CPU time bound instead of wall clock time
	// bound.  But these ideas are harder to implement.  We do not try to detect or handle a
	// user-specified -XX:+DisableExplicitGC.
	//
	// TODO(user): Consider using
	// java/lang/management/OperatingSystemMXBean.html#getSystemLoadAverage()
	//
	// TODO(user): Consider scaling by number of mutator threads,
	// e.g. using Thread#activeCount()
	return Math.max(10L, Runtime.getRuntime().totalMemory() / (32L * 1024L * 1024L));
    }

    /**
    * Creates a garbage object that counts down the latch in its finalizer. Sequestered into a
    * separate method to make it somewhat more likely to be unreachable.
    */
    private static void createUnreachableLatchFinalizer(final CountDownLatch latch) {
	new Object() {
	    @Override
	    protected void finalize() {
		latch.countDown();
	    }
	};
    }

    /**
    * Waits until the given latch has {@linkplain CountDownLatch#countDown counted down} to zero,
    * invoking the garbage collector as necessary to try to ensure that this will happen.
    *
    * @throws RuntimeException if timed out or interrupted while waiting
    */
    public static void await(CountDownLatch latch) {
	if (latch.getCount() == 0) {
	    return;
	}
	final long timeoutSeconds = timeoutSeconds();
	final long deadline = System.nanoTime() + SECONDS.toNanos(timeoutSeconds);
	do {
	    System.runFinalization();
	    if (latch.getCount() == 0) {
		return;
	    }
	    System.gc();
	    try {
		if (latch.await(1L, SECONDS)) {
		    return;
		}
	    } catch (InterruptedException ie) {
		throw new RuntimeException("Unexpected interrupt while waiting for latch", ie);
	    }
	} while (System.nanoTime() - deadline &lt; 0);
	throw formatRuntimeException("Latch failed to count down within %d second timeout", timeoutSeconds);
    }

    private static RuntimeException formatRuntimeException(String format, Object... args) {
	return new RuntimeException(String.format(Locale.ROOT, format, args));
    }

    interface FinalizationPredicate {
	boolean isDone();

    }

}

