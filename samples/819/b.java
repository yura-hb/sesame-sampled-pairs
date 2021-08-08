import java.util.concurrent.locks.AbstractQueuedSynchronizer;

class AbstractFutureBenchmarks {
    abstract class OldAbstractFuture&lt;V&gt; implements ListenableFuture&lt;V&gt; {
	class Sync&lt;V&gt; extends AbstractQueuedSynchronizer {
	    /** Checks if the state is {@link #INTERRUPTED}. */
	    boolean wasInterrupted() {
		return getState() == INTERRUPTED;
	    }

	    static final int INTERRUPTED = 8;

	}

    }

}

