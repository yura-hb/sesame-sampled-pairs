import java.util.Iterator;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.util.Util;

class Buffer implements IBuffer {
    /**
    * Notify the listeners that this buffer has changed.
    * To avoid deadlock, this should not be called in a synchronized block.
    */
    protected void notifyChanged(final BufferChangedEvent event) {
	ListenerList&lt;IBufferChangedListener&gt; listeners = this.changeListeners;
	if (listeners != null) {
	    Iterator&lt;IBufferChangedListener&gt; iterator = listeners.iterator();
	    while (iterator.hasNext()) {
		final IBufferChangedListener listener = iterator.next();
		SafeRunner.run(new ISafeRunnable() {
		    @Override
		    public void handleException(Throwable exception) {
			Util.log(exception, "Exception occurred in listener of buffer change notification"); //$NON-NLS-1$
		    }

		    @Override
		    public void run() throws Exception {
			listener.bufferChanged(event);
		    }
		});
	    }
	}
    }

    protected ListenerList&lt;IBufferChangedListener&gt; changeListeners;

}

