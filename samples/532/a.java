import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

class Queues {
    /**
    * Drains the queue as {@linkplain #drain(BlockingQueue, Collection, int, long, TimeUnit)}, but
    * with a different behavior in case it is interrupted while waiting. In that case, the operation
    * will continue as usual, and in the end the thread's interruption status will be set (no {@code
    * InterruptedException} is thrown).
    *
    * @param q the blocking queue to be drained
    * @param buffer where to add the transferred elements
    * @param numElements the number of elements to be waited for
    * @param timeout how long to wait before giving up, in units of {@code unit}
    * @param unit a {@code TimeUnit} determining how to interpret the timeout parameter
    * @return the number of elements transferred
    */
    @Beta
    @CanIgnoreReturnValue
    @GwtIncompatible // BlockingQueue
    public static &lt;E&gt; int drainUninterruptibly(BlockingQueue&lt;E&gt; q, Collection&lt;? super E&gt; buffer, int numElements,
	    long timeout, TimeUnit unit) {
	Preconditions.checkNotNull(buffer);
	long deadline = System.nanoTime() + unit.toNanos(timeout);
	int added = 0;
	boolean interrupted = false;
	try {
	    while (added &lt; numElements) {
		// we could rely solely on #poll, but #drainTo might be more efficient when there are
		// multiple elements already available (e.g. LinkedBlockingQueue#drainTo locks only once)
		added += q.drainTo(buffer, numElements - added);
		if (added &lt; numElements) { // not enough elements immediately available; will have to poll
		    E e; // written exactly once, by a successful (uninterrupted) invocation of #poll
		    while (true) {
			try {
			    e = q.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
			    break;
			} catch (InterruptedException ex) {
			    interrupted = true; // note interruption and retry
			}
		    }
		    if (e == null) {
			break; // we already waited enough, and there are no more elements in sight
		    }
		    buffer.add(e);
		    added++;
		}
	    }
	} finally {
	    if (interrupted) {
		Thread.currentThread().interrupt();
	    }
	}
	return added;
    }

}

