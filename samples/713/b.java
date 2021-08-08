import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.management.remote.NotificationResult;
import com.sun.jmx.remote.util.ClassLogger;
import com.sun.jmx.remote.util.EnvHelp;

abstract class ClientNotifForwarder {
    /**
     * Called after reconnection is finished.
     * This method is intended to be called only by a client connector:
     * &lt;code&gt;RMIConnector&lt;/code&gt; and &lt;code&gt;ClientIntermediary&lt;/code&gt;.
     */
    public synchronized void postReconnection(ClientListenerInfo[] listenerInfos) throws IOException {

	if (state == TERMINATED) {
	    return;
	}

	while (state == STOPPING) {
	    try {
		wait();
	    } catch (InterruptedException ire) {
		IOException ioe = new IOException(ire.toString());
		EnvHelp.initCause(ioe, ire);
		throw ioe;
	    }
	}

	final boolean trace = logger.traceOn();
	final int len = listenerInfos.length;

	for (int i = 0; i &lt; len; i++) {
	    if (trace) {
		logger.trace("addNotificationListeners", "Add a listener at " + listenerInfos[i].getListenerID());
	    }

	    infoList.put(listenerInfos[i].getListenerID(), listenerInfos[i]);
	}

	beingReconnected = false;
	notifyAll();

	if (currentFetchThread == Thread.currentThread() || state == STARTING || state == STARTED) { // doing or waiting reconnection
	    // only update mbeanRemovedNotifID
	    try {
		mbeanRemovedNotifID = addListenerForMBeanRemovedNotif();
	    } catch (Exception e) {
		final String msg = "Failed to register a listener to the mbean "
			+ "server: the client will not do clean when an MBean " + "is unregistered";
		if (logger.traceOn()) {
		    logger.trace("init", msg, e);
		}
	    }
	} else {
	    while (state == STOPPING) {
		try {
		    wait();
		} catch (InterruptedException ire) {
		    IOException ioe = new IOException(ire.toString());
		    EnvHelp.initCause(ioe, ire);
		    throw ioe;
		}
	    }

	    if (listenerInfos.length &gt; 0) { // old listeners are re-added
		init(true); // not update clientSequenceNumber
	    } else if (infoList.size() &gt; 0) { // only new listeners added during reconnection
		init(false); // need update clientSequenceNumber
	    }
	}
    }

    private int state = STOPPED;
    /**
     * This state means that this object is terminated and no more thread will be created
     * for fetching notifications.
     */
    private static final int TERMINATED = 4;
    /**
     * This state means that the fetching thread is informed to stop.
     */
    private static final int STOPPING = 2;
    private static final ClassLogger logger = new ClassLogger("javax.management.remote.misc", "ClientNotifForwarder");
    private final Map&lt;Integer, ClientListenerInfo&gt; infoList = new HashMap&lt;Integer, ClientListenerInfo&gt;();
    /**
     * This variable is used to tell whether a connector (RMIConnector or ClientIntermediary)
     * is doing reconnection.
     * This variable will be set to true by the method &lt;code&gt;preReconnection&lt;/code&gt;, and set
     * to false by &lt;code&gt;postReconnection&lt;/code&gt;.
     * When beingReconnected == true, no thread will be created for fetching notifications.
     */
    private boolean beingReconnected = false;
    private Thread currentFetchThread;
    /**
     * This state means that a thread is being created for fetching and forwarding notifications.
     */
    private static final int STARTING = 0;
    /**
     * This state tells that a thread has been started for fetching and forwarding notifications.
     */
    private static final int STARTED = 1;
    private Integer mbeanRemovedNotifID = null;
    /**
     * This state means that the fetching thread is already stopped.
     */
    private static final int STOPPED = 3;
    private long clientSequenceNumber = -1;
    private Executor executor;

    abstract protected Integer addListenerForMBeanRemovedNotif() throws IOException, InstanceNotFoundException;

    private synchronized void init(boolean reconnected) throws IOException {
	switch (state) {
	case STARTED:
	    return;
	case STARTING:
	    return;
	case TERMINATED:
	    throw new IOException("The ClientNotifForwarder has been terminated.");
	case STOPPING:
	    if (beingReconnected == true) {
		// wait for another thread to do, which is doing reconnection
		return;
	    }

	    while (state == STOPPING) { // make sure only one fetching thread.
		try {
		    wait();
		} catch (InterruptedException ire) {
		    IOException ioe = new IOException(ire.toString());
		    EnvHelp.initCause(ioe, ire);

		    throw ioe;
		}
	    }

	    // re-call this method to check the state again,
	    // the state can be other value like TERMINATED.
	    init(reconnected);

	    return;
	case STOPPED:
	    if (beingReconnected == true) {
		// wait for another thread to do, which is doing reconnection
		return;
	    }

	    if (logger.traceOn()) {
		logger.trace("init", "Initializing...");
	    }

	    // init the clientSequenceNumber if not reconnected.
	    if (!reconnected) {
		try {
		    NotificationResult nr = fetchNotifs(-1, 0, 0);

		    if (state != STOPPED) { // JDK-8038940
					    // reconnection must happen during
					    // fetchNotifs(-1, 0, 0), and a new
					    // thread takes over the fetching job
			return;
		    }

		    clientSequenceNumber = nr.getNextSequenceNumber();
		} catch (ClassNotFoundException e) {
		    // can't happen
		    logger.warning("init", "Impossible exception: " + e);
		    logger.debug("init", e);
		}
	    }

	    // for cleaning
	    try {
		mbeanRemovedNotifID = addListenerForMBeanRemovedNotif();
	    } catch (Exception e) {
		final String msg = "Failed to register a listener to the mbean "
			+ "server: the client will not do clean when an MBean " + "is unregistered";
		if (logger.traceOn()) {
		    logger.trace("init", msg, e);
		}
	    }

	    setState(STARTING);

	    // start fetching
	    executor.execute(new NotifFetcher());

	    return;
	default:
	    // should not
	    throw new IOException("Unknown state.");
	}
    }

    /**
     * Called to fetch notifications from a server.
     */
    abstract protected NotificationResult fetchNotifs(long clientSequenceNumber, int maxNotifications, long timeout)
	    throws IOException, ClassNotFoundException;

    private synchronized void setState(int newState) {
	if (state == TERMINATED) {
	    return;
	}

	state = newState;
	this.notifyAll();
    }

}

