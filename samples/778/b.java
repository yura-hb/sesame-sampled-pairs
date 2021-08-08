import java.awt.*;
import java.awt.event.InvocationEvent;

abstract class GlobalCursorManager {
    /**
     * Should be called in response to a native mouse enter or native mouse
     * button released message. Should not be called during a mouse drag.
     */
    public void updateCursorLater(Component heavy) {
	nativeUpdater.postIfNotPending(heavy, new InvocationEvent(Toolkit.getDefaultToolkit(), nativeUpdater));
    }

    /**
     * Use a singleton NativeUpdater for better performance. We cannot use
     * a singleton InvocationEvent because we want each event to have a fresh
     * timestamp.
     */
    private final NativeUpdater nativeUpdater = new NativeUpdater();

    class NativeUpdater implements Runnable {
	/**
	* Use a singleton NativeUpdater for better performance. We cannot use
	* a singleton InvocationEvent because we want each event to have a fresh
	* timestamp.
	*/
	private final NativeUpdater nativeUpdater = new NativeUpdater();

	public void postIfNotPending(Component heavy, InvocationEvent in) {
	    boolean shouldPost = false;
	    synchronized (this) {
		if (!pending) {
		    pending = shouldPost = true;
		}
	    }
	    if (shouldPost) {
		SunToolkit.postEvent(SunToolkit.targetToAppContext(heavy), in);
	    }
	}

    }

}

