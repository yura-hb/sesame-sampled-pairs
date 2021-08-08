import java.lang.invoke.VarHandle;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.concurrent.locks.ReentrantLock;

abstract class ForkJoinTask&lt;V&gt; implements Future&lt;V&gt;, Serializable {
    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an
     * exception
     * @throws InterruptedException if the current thread is not a
     * member of a ForkJoinPool and was interrupted while waiting
     * @throws TimeoutException if the wait timed out
     */
    public final V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
	int s;
	long nanos = unit.toNanos(timeout);
	if (Thread.interrupted())
	    throw new InterruptedException();
	if ((s = status) &gt;= 0 && nanos &gt; 0L) {
	    long d = System.nanoTime() + nanos;
	    long deadline = (d == 0L) ? 1L : d; // avoid 0
	    Thread t = Thread.currentThread();
	    if (t instanceof ForkJoinWorkerThread) {
		ForkJoinWorkerThread wt = (ForkJoinWorkerThread) t;
		s = wt.pool.awaitJoin(wt.workQueue, this, deadline);
	    } else if ((s = ((this instanceof CountedCompleter)
		    ? ForkJoinPool.common.externalHelpComplete((CountedCompleter&lt;?&gt;) this, 0)
		    : ForkJoinPool.common.tryExternalUnpush(this) ? doExec() : 0)) &gt;= 0) {
		long ns, ms; // measure in nanosecs, but wait in millisecs
		while ((s = status) &gt;= 0 && (ns = deadline - System.nanoTime()) &gt; 0L) {
		    if ((ms = TimeUnit.NANOSECONDS.toMillis(ns)) &gt; 0L
			    && (s = (int) STATUS.getAndBitwiseOr(this, SIGNAL)) &gt;= 0) {
			synchronized (this) {
			    if (status &gt;= 0)
				wait(ms); // OK to throw InterruptedException
			    else
				notifyAll();
			}
		    }
		}
	    }
	}
	if (s &gt;= 0)
	    throw new TimeoutException();
	else if ((s & THROWN) != 0)
	    throw new ExecutionException(getThrowableException());
	else if ((s & ABNORMAL) != 0)
	    throw new CancellationException();
	else
	    return getRawResult();
    }

    /**
     * The status field holds run control status bits packed into a
     * single int to ensure atomicity.  Status is initially zero, and
     * takes on nonnegative values until completed, upon which it
     * holds (sign bit) DONE, possibly with ABNORMAL (cancelled or
     * exceptional) and THROWN (in which case an exception has been
     * stored). Tasks with dependent blocked waiting joiners have the
     * SIGNAL bit set.  Completion of a task with SIGNAL set awakens
     * any waiters via notifyAll. (Waiters also help signal others
     * upon completion.)
     *
     * These control bits occupy only (some of) the upper half (16
     * bits) of status field. The lower bits are used for user-defined
     * tags.
     */
    volatile int status;
    private static final VarHandle STATUS;
    private static final int SIGNAL = 1 &lt;&lt; 16;
    private static final int THROWN = 1 &lt;&lt; 17;
    private static final int ABNORMAL = 1 &lt;&lt; 18;
    /** Lock protecting access to exceptionTable. */
    private static final ReentrantLock exceptionTableLock = new ReentrantLock();
    /**
     * Hash table of exceptions thrown by tasks, to enable reporting
     * by callers. Because exceptions are rare, we don't directly keep
     * them with task objects, but instead use a weak ref table.  Note
     * that cancellation exceptions don't appear in the table, but are
     * instead recorded as status values.
     *
     * The exception table has a fixed capacity.
     */
    private static final ExceptionNode[] exceptionTable = new ExceptionNode[32];
    private static final int DONE = 1 &lt;&lt; 31;
    /** Reference queue of stale exceptionally completed tasks. */
    private static final ReferenceQueue&lt;ForkJoinTask&lt;?&gt;&gt; exceptionTableRefQueue = new ReferenceQueue&lt;&gt;();

    /**
     * Primary execution method for stolen tasks. Unless done, calls
     * exec and records status if completed, but doesn't wait for
     * completion otherwise.
     *
     * @return status on exit from this method
     */
    final int doExec() {
	int s;
	boolean completed;
	if ((s = status) &gt;= 0) {
	    try {
		completed = exec();
	    } catch (Throwable rex) {
		completed = false;
		s = setExceptionalCompletion(rex);
	    }
	    if (completed)
		s = setDone();
	}
	return s;
    }

    /**
     * Returns a rethrowable exception for this task, if available.
     * To provide accurate stack traces, if the exception was not
     * thrown by the current thread, we try to create a new exception
     * of the same type as the one thrown, but with the recorded
     * exception as its cause. If there is no such constructor, we
     * instead try to use a no-arg constructor, followed by initCause,
     * to the same effect. If none of these apply, or any fail due to
     * other exceptions, we return the recorded exception, which is
     * still correct, although it may contain a misleading stack
     * trace.
     *
     * @return the exception, or null if none
     */
    private Throwable getThrowableException() {
	int h = System.identityHashCode(this);
	ExceptionNode e;
	final ReentrantLock lock = exceptionTableLock;
	lock.lock();
	try {
	    expungeStaleExceptions();
	    ExceptionNode[] t = exceptionTable;
	    e = t[h & (t.length - 1)];
	    while (e != null && e.get() != this)
		e = e.next;
	} finally {
	    lock.unlock();
	}
	Throwable ex;
	if (e == null || (ex = e.ex) == null)
	    return null;
	if (e.thrower != Thread.currentThread().getId()) {
	    try {
		Constructor&lt;?&gt; noArgCtor = null;
		// public ctors only
		for (Constructor&lt;?&gt; c : ex.getClass().getConstructors()) {
		    Class&lt;?&gt;[] ps = c.getParameterTypes();
		    if (ps.length == 0)
			noArgCtor = c;
		    else if (ps.length == 1 && ps[0] == Throwable.class)
			return (Throwable) c.newInstance(ex);
		}
		if (noArgCtor != null) {
		    Throwable wx = (Throwable) noArgCtor.newInstance();
		    wx.initCause(ex);
		    return wx;
		}
	    } catch (Exception ignore) {
	    }
	}
	return ex;
    }

    /**
     * Returns the result that would be returned by {@link #join}, even
     * if this task completed abnormally, or {@code null} if this task
     * is not known to have been completed.  This method is designed
     * to aid debugging, as well as to support extensions. Its use in
     * any other context is discouraged.
     *
     * @return the result, or {@code null} if not completed
     */
    public abstract V getRawResult();

    /**
     * Immediately performs the base action of this task and returns
     * true if, upon return from this method, this task is guaranteed
     * to have completed normally. This method may return false
     * otherwise, to indicate that this task is not necessarily
     * complete (or is not known to be complete), for example in
     * asynchronous actions that require explicit invocations of
     * completion methods. This method may also throw an (unchecked)
     * exception to indicate abnormal exit. This method is designed to
     * support extensions, and should not in general be called
     * otherwise.
     *
     * @return {@code true} if this task is known to have completed normally
     */
    protected abstract boolean exec();

    /**
     * Records exception and possibly propagates.
     *
     * @return status on exit
     */
    private int setExceptionalCompletion(Throwable ex) {
	int s = recordExceptionalCompletion(ex);
	if ((s & THROWN) != 0)
	    internalPropagateException(ex);
	return s;
    }

    /**
     * Sets DONE status and wakes up threads waiting to join this task.
     *
     * @return status on exit
     */
    private int setDone() {
	int s;
	if (((s = (int) STATUS.getAndBitwiseOr(this, DONE)) & SIGNAL) != 0)
	    synchronized (this) {
		notifyAll();
	    }
	return s | DONE;
    }

    /**
     * Polls stale refs and removes them. Call only while holding lock.
     */
    private static void expungeStaleExceptions() {
	for (Object x; (x = exceptionTableRefQueue.poll()) != null;) {
	    if (x instanceof ExceptionNode) {
		ExceptionNode[] t = exceptionTable;
		int i = ((ExceptionNode) x).hashCode & (t.length - 1);
		ExceptionNode e = t[i];
		ExceptionNode pred = null;
		while (e != null) {
		    ExceptionNode next = e.next;
		    if (e == x) {
			if (pred == null)
			    t[i] = next;
			else
			    pred.next = next;
			break;
		    }
		    pred = e;
		    e = next;
		}
	    }
	}
    }

    /**
     * Records exception and sets status.
     *
     * @return status on exit
     */
    final int recordExceptionalCompletion(Throwable ex) {
	int s;
	if ((s = status) &gt;= 0) {
	    int h = System.identityHashCode(this);
	    final ReentrantLock lock = exceptionTableLock;
	    lock.lock();
	    try {
		expungeStaleExceptions();
		ExceptionNode[] t = exceptionTable;
		int i = h & (t.length - 1);
		for (ExceptionNode e = t[i];; e = e.next) {
		    if (e == null) {
			t[i] = new ExceptionNode(this, ex, t[i], exceptionTableRefQueue);
			break;
		    }
		    if (e.get() == this) // already present
			break;
		}
	    } finally {
		lock.unlock();
	    }
	    s = abnormalCompletion(DONE | ABNORMAL | THROWN);
	}
	return s;
    }

    /**
     * Hook for exception propagation support for tasks with completers.
     */
    void internalPropagateException(Throwable ex) {
    }

    /**
     * Marks cancelled or exceptional completion unless already done.
     *
     * @param completion must be DONE | ABNORMAL, ORed with THROWN if exceptional
     * @return status on exit
     */
    private int abnormalCompletion(int completion) {
	for (int s, ns;;) {
	    if ((s = status) &lt; 0)
		return s;
	    else if (STATUS.weakCompareAndSet(this, s, ns = s | completion)) {
		if ((s & SIGNAL) != 0)
		    synchronized (this) {
			notifyAll();
		    }
		return ns;
	    }
	}
    }

    class ExceptionNode extends WeakReference&lt;ForkJoinTask&lt;?&gt;&gt; {
	/**
	* The status field holds run control status bits packed into a
	* single int to ensure atomicity.  Status is initially zero, and
	* takes on nonnegative values until completed, upon which it
	* holds (sign bit) DONE, possibly with ABNORMAL (cancelled or
	* exceptional) and THROWN (in which case an exception has been
	* stored). Tasks with dependent blocked waiting joiners have the
	* SIGNAL bit set.  Completion of a task with SIGNAL set awakens
	* any waiters via notifyAll. (Waiters also help signal others
	* upon completion.)
	*
	* These control bits occupy only (some of) the upper half (16
	* bits) of status field. The lower bits are used for user-defined
	* tags.
	*/
	volatile int status;
	private static final VarHandle STATUS;
	private static final int SIGNAL = 1 &lt;&lt; 16;
	private static final int THROWN = 1 &lt;&lt; 17;
	private static final int ABNORMAL = 1 &lt;&lt; 18;
	/** Lock protecting access to exceptionTable. */
	private static final ReentrantLock exceptionTableLock = new ReentrantLock();
	/**
	* Hash table of exceptions thrown by tasks, to enable reporting
	* by callers. Because exceptions are rare, we don't directly keep
	* them with task objects, but instead use a weak ref table.  Note
	* that cancellation exceptions don't appear in the table, but are
	* instead recorded as status values.
	*
	* The exception table has a fixed capacity.
	*/
	private static final ExceptionNode[] exceptionTable = new ExceptionNode[32];
	private static final int DONE = 1 &lt;&lt; 31;
	/** Reference queue of stale exceptionally completed tasks. */
	private static final ReferenceQueue&lt;ForkJoinTask&lt;?&gt;&gt; exceptionTableRefQueue = new ReferenceQueue&lt;&gt;();

	ExceptionNode(ForkJoinTask&lt;?&gt; task, Throwable ex, ExceptionNode next,
		ReferenceQueue&lt;ForkJoinTask&lt;?&gt;&gt; exceptionTableRefQueue) {
	    super(task, exceptionTableRefQueue);
	    this.ex = ex;
	    this.next = next;
	    this.thrower = Thread.currentThread().getId();
	    this.hashCode = System.identityHashCode(task);
	}

    }

}

