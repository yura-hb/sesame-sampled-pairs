import java.lang.invoke.VarHandle;
import java.util.concurrent.locks.LockSupport;

class ForkJoinPool extends AbstractExecutorService {
    class WorkQueue {
	/**
	 * Pushes a task. Call only by owner in unshared queues.
	 *
	 * @param task the task. Caller must ensure non-null.
	 * @throws RejectedExecutionException if array cannot be resized
	 */
	final void push(ForkJoinTask&lt;?&gt; task) {
	    ForkJoinTask&lt;?&gt;[] a;
	    int s = top, d, cap, m;
	    ForkJoinPool p = pool;
	    if ((a = array) != null && (cap = a.length) &gt; 0) {
		QA.setRelease(a, (m = cap - 1) & s, task);
		top = s + 1;
		if (((d = s - (int) BASE.getAcquire(this)) & ~1) == 0 && p != null) { // size 0 or 1
		    VarHandle.fullFence();
		    p.signalWork();
		} else if (d == m)
		    growArray(false);
	    }
	}

	int top;
	final ForkJoinPool pool;
	ForkJoinTask&lt;?&gt;[] array;
	static final VarHandle BASE;
	volatile int phase;
	int stackPred;
	final ForkJoinWorkerThread owner;
	volatile int source;
	int id;
	int nsteals;
	int base;

	/**
	 * Doubles the capacity of array. Call either by owner or with
	 * lock held -- it is OK for base, but not top, to move while
	 * resizings are in progress.
	 */
	final void growArray(boolean locked) {
	    ForkJoinTask&lt;?&gt;[] newA = null;
	    try {
		ForkJoinTask&lt;?&gt;[] oldA;
		int oldSize, newSize;
		if ((oldA = array) != null && (oldSize = oldA.length) &gt; 0
			&& (newSize = oldSize &lt;&lt; 1) &lt;= MAXIMUM_QUEUE_CAPACITY && newSize &gt; 0) {
		    try {
			newA = new ForkJoinTask&lt;?&gt;[newSize];
		    } catch (OutOfMemoryError ex) {
		    }
		    if (newA != null) { // poll from old array, push to new
			int oldMask = oldSize - 1, newMask = newSize - 1;
			for (int s = top - 1, k = oldMask; k &gt;= 0; --k) {
			    ForkJoinTask&lt;?&gt; x = (ForkJoinTask&lt;?&gt;) QA.getAndSet(oldA, s & oldMask, null);
			    if (x != null)
				newA[s-- & newMask] = x;
			    else
				break;
			}
			array = newA;
			VarHandle.releaseFence();
		    }
		}
	    } finally {
		if (locked)
		    phase = 0;
	    }
	    if (newA == null)
		throw new RejectedExecutionException("Queue capacity exceeded");
	}

	/**
	 * Removes and cancels all known tasks, ignoring any exceptions.
	 */
	final void cancelAll() {
	    for (ForkJoinTask&lt;?&gt; t; (t = poll()) != null;)
		ForkJoinTask.cancelIgnoringExceptions(t);
	}

	/**
	 * Takes next task, if one exists, in FIFO order.
	 */
	final ForkJoinTask&lt;?&gt; poll() {
	    int b, k, cap;
	    ForkJoinTask&lt;?&gt;[] a;
	    while ((a = array) != null && (cap = a.length) &gt; 0 && top - (b = base) &gt; 0) {
		ForkJoinTask&lt;?&gt; t = (ForkJoinTask&lt;?&gt;) QA.getAcquire(a, k = (cap - 1) & b);
		if (base == b++) {
		    if (t == null)
			Thread.yield(); // await index advance
		    else if (QA.compareAndSet(a, k, t, null)) {
			BASE.setOpaque(this, b);
			return t;
		    }
		}
	    }
	    return null;
	}

    }

    static final VarHandle QA;
    @jdk.internal.vm.annotation.Contended("fjpctl") // segregate
    volatile long ctl;
    private static final long ADD_WORKER = 0x0001L &lt;&lt; (TC_SHIFT + 15);
    WorkQueue[] workQueues;
    static final int SMASK = 0xffff;
    static final int UNSIGNALLED = 1 &lt;&lt; 31;
    private static final long SP_MASK = 0xffffffffL;
    private static final long UC_MASK = ~SP_MASK;
    private static final long RC_UNIT = 0x0001L &lt;&lt; RC_SHIFT;
    private static final VarHandle CTL;
    /**
     * Maximum capacity for queue arrays. Must be a power of two less
     * than or equal to 1 &lt;&lt; (31 - width of array entry) to ensure
     * lack of wraparound of index calculations, but defined to a
     * value a bit less than this to help users trap runaway programs
     * before saturating systems.
     */
    static final int MAXIMUM_QUEUE_CAPACITY = 1 &lt;&lt; 26;
    private static final long RC_MASK = 0xffffL &lt;&lt; RC_SHIFT;
    private static final long TC_MASK = 0xffffL &lt;&lt; TC_SHIFT;
    private static final long TC_UNIT = 0x0001L &lt;&lt; TC_SHIFT;
    final ForkJoinWorkerThreadFactory factory;
    final String workerNamePrefix;
    volatile long stealCount;
    static final int QUIET = 1 &lt;&lt; 30;
    volatile int mode;
    static final int SHUTDOWN = 1 &lt;&lt; 18;
    /**
     * Common (static) pool. Non-null for public use unless a static
     * construction exception, but internal usages null-check on use
     * to paranoically avoid potential initialization circularities
     * as well as to simplify generated code.
     */
    static final ForkJoinPool common;
    private static final VarHandle MODE;
    static final int STOP = 1 &lt;&lt; 31;
    private static final int RC_SHIFT = 48;
    static final int TERMINATED = 1 &lt;&lt; 19;
    private static final int TC_SHIFT = 32;

    /**
     * Tries to create or release a worker if too few are running.
     */
    final void signalWork() {
	for (;;) {
	    long c;
	    int sp;
	    WorkQueue[] ws;
	    int i;
	    WorkQueue v;
	    if ((c = ctl) &gt;= 0L) // enough workers
		break;
	    else if ((sp = (int) c) == 0) { // no idle workers
		if ((c & ADD_WORKER) != 0L) // too few workers
		    tryAddWorker(c);
		break;
	    } else if ((ws = workQueues) == null)
		break; // unstarted/terminated
	    else if (ws.length &lt;= (i = sp & SMASK))
		break; // terminated
	    else if ((v = ws[i]) == null)
		break; // terminating
	    else {
		int np = sp & ~UNSIGNALLED;
		int vp = v.phase;
		long nc = (v.stackPred & SP_MASK) | (UC_MASK & (c + RC_UNIT));
		Thread vt = v.owner;
		if (sp == vp && CTL.compareAndSet(this, c, nc)) {
		    v.phase = np;
		    if (vt != null && v.source &lt; 0)
			LockSupport.unpark(vt);
		    break;
		}
	    }
	}
    }

    /**
     * Tries to add one worker, incrementing ctl counts before doing
     * so, relying on createWorker to back out on failure.
     *
     * @param c incoming ctl value, with total count negative and no
     * idle workers.  On CAS failure, c is refreshed and retried if
     * this holds (otherwise, a new worker is not needed).
     */
    private void tryAddWorker(long c) {
	do {
	    long nc = ((RC_MASK & (c + RC_UNIT)) | (TC_MASK & (c + TC_UNIT)));
	    if (ctl == c && CTL.compareAndSet(this, c, nc)) {
		createWorker();
		break;
	    }
	} while (((c = ctl) & ADD_WORKER) != 0L && (int) c == 0);
    }

    /**
     * Tries to construct and start one worker. Assumes that total
     * count has already been incremented as a reservation.  Invokes
     * deregisterWorker on any failure.
     *
     * @return true if successful
     */
    private boolean createWorker() {
	ForkJoinWorkerThreadFactory fac = factory;
	Throwable ex = null;
	ForkJoinWorkerThread wt = null;
	try {
	    if (fac != null && (wt = fac.newThread(this)) != null) {
		wt.start();
		return true;
	    }
	} catch (Throwable rex) {
	    ex = rex;
	}
	deregisterWorker(wt, ex);
	return false;
    }

    /**
     * Final callback from terminating worker, as well as upon failure
     * to construct or start a worker.  Removes record of worker from
     * array, and adjusts counts. If pool is shutting down, tries to
     * complete termination.
     *
     * @param wt the worker thread, or null if construction failed
     * @param ex the exception causing failure, or null if none
     */
    final void deregisterWorker(ForkJoinWorkerThread wt, Throwable ex) {
	WorkQueue w = null;
	int phase = 0;
	if (wt != null && (w = wt.workQueue) != null) {
	    Object lock = workerNamePrefix;
	    int wid = w.id;
	    long ns = (long) w.nsteals & 0xffffffffL;
	    if (lock != null) {
		synchronized (lock) {
		    WorkQueue[] ws;
		    int n, i; // remove index from array
		    if ((ws = workQueues) != null && (n = ws.length) &gt; 0 && ws[i = wid & (n - 1)] == w)
			ws[i] = null;
		    stealCount += ns;
		}
	    }
	    phase = w.phase;
	}
	if (phase != QUIET) { // else pre-adjusted
	    long c; // decrement counts
	    do {
	    } while (!CTL.weakCompareAndSet(this, c = ctl,
		    ((RC_MASK & (c - RC_UNIT)) | (TC_MASK & (c - TC_UNIT)) | (SP_MASK & c))));
	}
	if (w != null)
	    w.cancelAll(); // cancel remaining tasks

	if (!tryTerminate(false, false) && // possibly replace worker
		w != null && w.array != null) // avoid repeated failures
	    signalWork();

	if (ex == null) // help clean on way out
	    ForkJoinTask.helpExpungeStaleExceptions();
	else // rethrow
	    ForkJoinTask.rethrow(ex);
    }

    /**
     * Possibly initiates and/or completes termination.
     *
     * @param now if true, unconditionally terminate, else only
     * if no work and no active workers
     * @param enable if true, terminate when next possible
     * @return true if terminating or terminated
     */
    private boolean tryTerminate(boolean now, boolean enable) {
	int md; // 3 phases: try to set SHUTDOWN, then STOP, then TERMINATED

	while (((md = mode) & SHUTDOWN) == 0) {
	    if (!enable || this == common) // cannot shutdown
		return false;
	    else
		MODE.compareAndSet(this, md, md | SHUTDOWN);
	}

	while (((md = mode) & STOP) == 0) { // try to initiate termination
	    if (!now) { // check if quiescent & empty
		for (long oldSum = 0L;;) { // repeat until stable
		    boolean running = false;
		    long checkSum = ctl;
		    WorkQueue[] ws = workQueues;
		    if ((md & SMASK) + (int) (checkSum &gt;&gt; RC_SHIFT) &gt; 0)
			running = true;
		    else if (ws != null) {
			WorkQueue w;
			for (int i = 0; i &lt; ws.length; ++i) {
			    if ((w = ws[i]) != null) {
				int s = w.source, p = w.phase;
				int d = w.id, b = w.base;
				if (b != w.top || ((d & 1) == 1 && (s &gt;= 0 || p &gt;= 0))) {
				    running = true;
				    break; // working, scanning, or have work
				}
				checkSum += (((long) s &lt;&lt; 48) + ((long) p &lt;&lt; 32) + ((long) b &lt;&lt; 16) + (long) d);
			    }
			}
		    }
		    if (((md = mode) & STOP) != 0)
			break; // already triggered
		    else if (running)
			return false;
		    else if (workQueues == ws && oldSum == (oldSum = checkSum))
			break;
		}
	    }
	    if ((md & STOP) == 0)
		MODE.compareAndSet(this, md, md | STOP);
	}

	while (((md = mode) & TERMINATED) == 0) { // help terminate others
	    for (long oldSum = 0L;;) { // repeat until stable
		WorkQueue[] ws;
		WorkQueue w;
		long checkSum = ctl;
		if ((ws = workQueues) != null) {
		    for (int i = 0; i &lt; ws.length; ++i) {
			if ((w = ws[i]) != null) {
			    ForkJoinWorkerThread wt = w.owner;
			    w.cancelAll(); // clear queues
			    if (wt != null) {
				try { // unblock join or park
				    wt.interrupt();
				} catch (Throwable ignore) {
				}
			    }
			    checkSum += ((long) w.phase &lt;&lt; 32) + w.base;
			}
		    }
		}
		if (((md = mode) & TERMINATED) != 0 || (workQueues == ws && oldSum == (oldSum = checkSum)))
		    break;
	    }
	    if ((md & TERMINATED) != 0)
		break;
	    else if ((md & SMASK) + (short) (ctl &gt;&gt;&gt; TC_SHIFT) &gt; 0)
		break;
	    else if (MODE.compareAndSet(this, md, md | TERMINATED)) {
		synchronized (this) {
		    notifyAll(); // for awaitTermination
		}
		break;
	    }
	}
	return true;
    }

    interface ForkJoinWorkerThreadFactory {
	static final VarHandle QA;
	@jdk.internal.vm.annotation.Contended("fjpctl") // segregate
	volatile long ctl;
	private static final long ADD_WORKER = 0x0001L &lt;&lt; (TC_SHIFT + 15);
	WorkQueue[] workQueues;
	static final int SMASK = 0xffff;
	static final int UNSIGNALLED = 1 &lt;&lt; 31;
	private static final long SP_MASK = 0xffffffffL;
	private static final long UC_MASK = ~SP_MASK;
	private static final long RC_UNIT = 0x0001L &lt;&lt; RC_SHIFT;
	private static final VarHandle CTL;
	/**
	* Maximum capacity for queue arrays. Must be a power of two less
	* than or equal to 1 &lt;&lt; (31 - width of array entry) to ensure
	* lack of wraparound of index calculations, but defined to a
	* value a bit less than this to help users trap runaway programs
	* before saturating systems.
	*/
	static final int MAXIMUM_QUEUE_CAPACITY = 1 &lt;&lt; 26;
	private static final long RC_MASK = 0xffffL &lt;&lt; RC_SHIFT;
	private static final long TC_MASK = 0xffffL &lt;&lt; TC_SHIFT;
	private static final long TC_UNIT = 0x0001L &lt;&lt; TC_SHIFT;
	final ForkJoinWorkerThreadFactory factory;
	final String workerNamePrefix;
	volatile long stealCount;
	static final int QUIET = 1 &lt;&lt; 30;
	volatile int mode;
	static final int SHUTDOWN = 1 &lt;&lt; 18;
	/**
	* Common (static) pool. Non-null for public use unless a static
	* construction exception, but internal usages null-check on use
	* to paranoically avoid potential initialization circularities
	* as well as to simplify generated code.
	*/
	static final ForkJoinPool common;
	private static final VarHandle MODE;
	static final int STOP = 1 &lt;&lt; 31;
	private static final int RC_SHIFT = 48;
	static final int TERMINATED = 1 &lt;&lt; 19;
	private static final int TC_SHIFT = 32;

	/**
	 * Returns a new worker thread operating in the given pool.
	 * Returning null or throwing an exception may result in tasks
	 * never being executed.  If this method throws an exception,
	 * it is relayed to the caller of the method (for example
	 * {@code execute}) causing attempted thread creation. If this
	 * method returns null or throws an exception, it is not
	 * retried until the next attempted creation (for example
	 * another call to {@code execute}).
	 *
	 * @param pool the pool this thread works in
	 * @return the new worker thread, or {@code null} if the request
	 *         to create a thread is rejected
	 * @throws NullPointerException if the pool is null
	 */
	public ForkJoinWorkerThread newThread(ForkJoinPool pool);

    }

}

