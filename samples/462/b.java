import java.nio.file.*;
import java.util.*;

abstract class AbstractWatchKey implements WatchKey {
    /**
     * Adds the event to this key and signals it.
     */
    @SuppressWarnings("unchecked")
    final void signalEvent(WatchEvent.Kind&lt;?&gt; kind, Object context) {
	boolean isModify = (kind == StandardWatchEventKinds.ENTRY_MODIFY);
	synchronized (this) {
	    int size = events.size();
	    if (size &gt; 0) {
		// if the previous event is an OVERFLOW event or this is a
		// repeated event then we simply increment the counter
		WatchEvent&lt;?&gt; prev = events.get(size - 1);
		if ((prev.kind() == StandardWatchEventKinds.OVERFLOW)
			|| ((kind == prev.kind() && Objects.equals(context, prev.context())))) {
		    ((Event&lt;?&gt;) prev).increment();
		    return;
		}

		// if this is a modify event and the last entry for the context
		// is a modify event then we simply increment the count
		if (!lastModifyEvents.isEmpty()) {
		    if (isModify) {
			WatchEvent&lt;?&gt; ev = lastModifyEvents.get(context);
			if (ev != null) {
			    assert ev.kind() == StandardWatchEventKinds.ENTRY_MODIFY;
			    ((Event&lt;?&gt;) ev).increment();
			    return;
			}
		    } else {
			// not a modify event so remove from the map as the
			// last event will no longer be a modify event.
			lastModifyEvents.remove(context);
		    }
		}

		// if the list has reached the limit then drop pending events
		// and queue an OVERFLOW event
		if (size &gt;= MAX_EVENT_LIST_SIZE) {
		    kind = StandardWatchEventKinds.OVERFLOW;
		    isModify = false;
		    context = null;
		}
	    }

	    // non-repeated event
	    Event&lt;Object&gt; ev = new Event&lt;&gt;((WatchEvent.Kind&lt;Object&gt;) kind, context);
	    if (isModify) {
		lastModifyEvents.put(context, ev);
	    } else if (kind == StandardWatchEventKinds.OVERFLOW) {
		// drop all pending events
		events.clear();
		lastModifyEvents.clear();
	    }
	    events.add(ev);
	    signal();
	}
    }

    private List&lt;WatchEvent&lt;?&gt;&gt; events;
    private Map&lt;Object, WatchEvent&lt;?&gt;&gt; lastModifyEvents;
    /**
     * Maximum size of event list (in the future this may be tunable)
     */
    static final int MAX_EVENT_LIST_SIZE = 512;
    private State state;
    private final AbstractWatchService watcher;

    /**
     * Enqueues this key to the watch service
     */
    final void signal() {
	synchronized (this) {
	    if (state == State.READY) {
		state = State.SIGNALLED;
		watcher.enqueueKey(this);
	    }
	}
    }

    class Event&lt;T&gt; implements WatchEvent&lt;T&gt; {
	private List&lt;WatchEvent&lt;?&gt;&gt; events;
	private Map&lt;Object, WatchEvent&lt;?&gt;&gt; lastModifyEvents;
	/**
	* Maximum size of event list (in the future this may be tunable)
	*/
	static final int MAX_EVENT_LIST_SIZE = 512;
	private State state;
	private final AbstractWatchService watcher;

	void increment() {
	    count++;
	}

	Event(WatchEvent.Kind&lt;T&gt; type, T context) {
	    this.kind = type;
	    this.context = context;
	    this.count = 1;
	}

    }

}

