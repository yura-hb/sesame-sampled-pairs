import com.google.common.util.concurrent.CheckedFuture;

abstract class AbstractCheckedFutureTest extends AbstractListenableFutureTest {
    /**
    * Tests that the {@link CheckedFuture#checkedGet()} method throws the correct type of
    * cancellation exception when it is cancelled.
    */
    public void testCheckedGetThrowsApplicationExceptionOnCancellation() {

	final CheckedFuture&lt;Boolean, ?&gt; future = createCheckedFuture(Boolean.TRUE, null, latch);

	assertFalse(future.isDone());
	assertFalse(future.isCancelled());

	new Thread(new Runnable() {
	    @Override
	    public void run() {
		future.cancel(true);
	    }
	}).start();

	try {
	    future.checkedGet();
	    fail("RPC Should have been cancelled.");
	} catch (Exception e) {
	    checkCancelledException(e);
	}

	assertTrue(future.isDone());
	assertTrue(future.isCancelled());
    }

    /** More specific type for the create method. */
    protected abstract &lt;V&gt; CheckedFuture&lt;V, ?&gt; createCheckedFuture(V value, Exception except, CountDownLatch waitOn);

    /** Checks that the exception is the correct type of cancellation exception. */
    protected abstract void checkCancelledException(Exception e);

}

