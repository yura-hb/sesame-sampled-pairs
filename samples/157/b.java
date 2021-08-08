import java.util.Vector;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.ldap.UnsolicitedNotificationEvent;

class EventQueue implements Runnable {
    /**
     * Pull events off the queue and dispatch them.
     */
    public void run() {
	QueueElement qe;

	try {
	    while ((qe = dequeue()) != null) {
		EventObject e = qe.event;
		Vector&lt;NamingListener&gt; v = qe.vector;

		for (int i = 0; i &lt; v.size(); i++) {

		    // Dispatch to corresponding NamingListener
		    // The listener should only be getting the event that
		    // it is interested in. (No need to check mask or
		    // instanceof subinterfaces.)
		    // It is the responsibility of the enqueuer to
		    // only enqueue events with listeners of the correct type.

		    if (e instanceof NamingEvent) {
			((NamingEvent) e).dispatch(v.elementAt(i));

			// An exception occurred: if notify all naming listeners
		    } else if (e instanceof NamingExceptionEvent) {
			((NamingExceptionEvent) e).dispatch(v.elementAt(i));
		    } else if (e instanceof UnsolicitedNotificationEvent) {
			((UnsolicitedNotificationEvent) e).dispatch((UnsolicitedNotificationListener) v.elementAt(i));
		    }
		}

		qe = null;
		e = null;
		v = null;
	    }
	} catch (InterruptedException e) {
	    // just die
	}
    }

    private QueueElement tail = null;
    private QueueElement head = null;

    /**
     * Dequeue the oldest object on the queue.
     * Used only by the run() method.
     *
     * @return    the oldest object on the queue.
     * @exception java.lang.InterruptedException if any thread has
     *              interrupted this thread.
     */
    private synchronized QueueElement dequeue() throws InterruptedException {
	while (tail == null)
	    wait();
	QueueElement elt = tail;
	tail = elt.prev;
	if (tail == null) {
	    head = null;
	} else {
	    tail.next = null;
	}
	elt.prev = elt.next = null;
	return elt;
    }

}

