import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

class Finalizer implements Runnable {
    /** Loops continuously, pulling references off the queue and cleaning them up. */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
	while (true) {
	    try {
		if (!cleanUp(queue.remove())) {
		    break;
		}
	    } catch (InterruptedException e) {
		// ignore
	    }
	}
    }

    private final ReferenceQueue&lt;Object&gt; queue;
    private final PhantomReference&lt;Object&gt; frqReference;
    private static final Logger logger = Logger.getLogger(Finalizer.class.getName());
    private final WeakReference&lt;Class&lt;?&gt;&gt; finalizableReferenceClassReference;

    /**
    * Cleans up a single reference. Catches and logs all throwables.
    *
    * @return true if the caller should continue, false if the associated FinalizableReferenceQueue
    *     is no longer referenced.
    */
    private boolean cleanUp(Reference&lt;?&gt; reference) {
	Method finalizeReferentMethod = getFinalizeReferentMethod();
	if (finalizeReferentMethod == null) {
	    return false;
	}
	do {
	    /*
	     * This is for the benefit of phantom references. Weak and soft references will have already
	     * been cleared by this point.
	     */
	    reference.clear();

	    if (reference == frqReference) {
		/*
		 * The client no longer has a reference to the FinalizableReferenceQueue. We can stop.
		 */
		return false;
	    }

	    try {
		finalizeReferentMethod.invoke(reference);
	    } catch (Throwable t) {
		logger.log(Level.SEVERE, "Error cleaning up after reference.", t);
	    }

	    /*
	     * Loop as long as we have references available so as not to waste CPU looking up the Method
	     * over and over again.
	     */
	} while ((reference = queue.poll()) != null);
	return true;
    }

    /** Looks up FinalizableReference.finalizeReferent() method. */
    private @Nullable Method getFinalizeReferentMethod() {
	Class&lt;?&gt; finalizableReferenceClass = finalizableReferenceClassReference.get();
	if (finalizableReferenceClass == null) {
	    /*
	     * FinalizableReference's class loader was reclaimed. While there's a chance that other
	     * finalizable references could be enqueued subsequently (at which point the class loader
	     * would be resurrected by virtue of us having a strong reference to it), we should pretty
	     * much just shut down and make sure we don't keep it alive any longer than necessary.
	     */
	    return null;
	}
	try {
	    return finalizableReferenceClass.getMethod("finalizeReferent");
	} catch (NoSuchMethodException e) {
	    throw new AssertionError(e);
	}
    }

}

