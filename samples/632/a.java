import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

class MoreExecutors {
    /**
    * Shuts down the given executor service gradually, first disabling new submissions and later, if
    * necessary, cancelling remaining tasks.
    *
    * &lt;p&gt;The method takes the following steps:
    *
    * &lt;ol&gt;
    *   &lt;li&gt;calls {@link ExecutorService#shutdown()}, disabling acceptance of new submitted tasks.
    *   &lt;li&gt;awaits executor service termination for half of the specified timeout.
    *   &lt;li&gt;if the timeout expires, it calls {@link ExecutorService#shutdownNow()}, cancelling
    *       pending tasks and interrupting running tasks.
    *   &lt;li&gt;awaits executor service termination for the other half of the specified timeout.
    * &lt;/ol&gt;
    *
    * &lt;p&gt;If, at any step of the process, the calling thread is interrupted, the method calls {@link
    * ExecutorService#shutdownNow()} and returns.
    *
    * @param service the {@code ExecutorService} to shut down
    * @param timeout the maximum time to wait for the {@code ExecutorService} to terminate
    * @param unit the time unit of the timeout argument
    * @return {@code true} if the {@code ExecutorService} was terminated successfully, {@code false}
    *     if the call timed out or was interrupted
    * @since 17.0
    */
    @Beta
    @CanIgnoreReturnValue
    @GwtIncompatible // concurrency
    public static boolean shutdownAndAwaitTermination(ExecutorService service, long timeout, TimeUnit unit) {
	long halfTimeoutNanos = unit.toNanos(timeout) / 2;
	// Disable new tasks from being submitted
	service.shutdown();
	try {
	    // Wait for half the duration of the timeout for existing tasks to terminate
	    if (!service.awaitTermination(halfTimeoutNanos, TimeUnit.NANOSECONDS)) {
		// Cancel currently executing tasks
		service.shutdownNow();
		// Wait the other half of the timeout for tasks to respond to being cancelled
		service.awaitTermination(halfTimeoutNanos, TimeUnit.NANOSECONDS);
	    }
	} catch (InterruptedException ie) {
	    // Preserve interrupt status
	    Thread.currentThread().interrupt();
	    // (Re-)Cancel if current thread also interrupted
	    service.shutdownNow();
	}
	return service.isTerminated();
    }

}

