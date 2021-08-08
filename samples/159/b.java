import sun.awt.*;
import java.util.concurrent.locks.Lock;

class EventQueue {
    /**
     * Returns the first event on the {@code EventQueue}
     * without removing it.
     * @return the first event
     */
    public AWTEvent peekEvent() {
	pushPopLock.lock();
	try {
	    for (int i = NUM_PRIORITIES - 1; i &gt;= 0; i--) {
		if (queues[i].head != null) {
		    return queues[i].head.event;
		}
	    }
	} finally {
	    pushPopLock.unlock();
	}

	return null;
    }

    private final Lock pushPopLock;
    private static final int NUM_PRIORITIES = ULTIMATE_PRIORITY + 1;
    private Queue[] queues = new Queue[NUM_PRIORITIES];

}

