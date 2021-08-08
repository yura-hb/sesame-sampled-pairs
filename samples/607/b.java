import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.RenderQueue;

class D3DRenderQueue extends RenderQueue {
    /**
     * Returns the single D3DRenderQueue instance.  If it has not yet been
     * initialized, this method will first construct the single instance
     * before returning it.
     */
    public static synchronized D3DRenderQueue getInstance() {
	if (theInstance == null) {
	    theInstance = new D3DRenderQueue();
	    // no need to lock, noone has reference to this instance yet
	    theInstance.flushAndInvokeNow(new Runnable() {
		public void run() {
		    rqThread = Thread.currentThread();
		}
	    });
	}
	return theInstance;
    }

    private static D3DRenderQueue theInstance;
    private static Thread rqThread;

    private D3DRenderQueue() {
    }

    public void flushAndInvokeNow(Runnable r) {
	// assert lock.isHeldByCurrentThread();
	flushBuffer(r);
    }

    private void flushBuffer(Runnable task) {
	// assert lock.isHeldByCurrentThread();
	int limit = buf.position();
	if (limit &gt; 0 || task != null) {
	    // process the queue
	    flushBuffer(buf.getAddress(), limit, task);
	}
	// reset the buffer position
	buf.clear();
	// clear the set of references, since we no longer need them
	refSet.clear();
    }

    private native void flushBuffer(long buf, int limit, Runnable task);

}

