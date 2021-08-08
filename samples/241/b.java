import java.util.*;
import nsk.share.*;

class EventHandler implements Runnable {
    /**
     * Removes the listener from the list.
     */
    public void removeListener(EventListener listener) {
	display("Removing listener " + listener);
	synchronized (listeners) {
	    listeners.remove(listener);
	}
    }

    /**
     * Container for event listeners
     */
    private static List&lt;EventListener&gt; listeners = Collections.synchronizedList(new Vector&lt;EventListener&gt;());
    private Log log = null;

    private void display(String str) {
	log.display("EventHandler&gt; " + str);
    }

}

