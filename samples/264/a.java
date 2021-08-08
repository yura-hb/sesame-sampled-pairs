import java.util.List;
import org.apache.commons.lang3.Validate;

class EventListenerSupport&lt;L&gt; implements Serializable {
    /**
     * Unregisters an event listener.
     *
     * @param listener the event listener (may not be &lt;code&gt;null&lt;/code&gt;).
     *
     * @throws NullPointerException if &lt;code&gt;listener&lt;/code&gt; is
     *         &lt;code&gt;null&lt;/code&gt;.
     */
    public void removeListener(final L listener) {
	Validate.notNull(listener, "Listener object cannot be null.");
	listeners.remove(listener);
    }

    /**
     * The list used to hold the registered listeners. This list is
     * intentionally a thread-safe copy-on-write-array so that traversals over
     * the list of listeners will be atomic.
     */
    private List&lt;L&gt; listeners = new CopyOnWriteArrayList&lt;&gt;();

}

