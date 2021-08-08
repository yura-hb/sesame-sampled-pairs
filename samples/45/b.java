import javax.swing.event.*;

abstract class AbstractDocument implements Document, Serializable {
    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param e the event
     * @see EventListenerList
     */
    protected void fireChangedUpdate(DocumentEvent e) {
	notifyingListeners = true;
	try {
	    // Guaranteed to return a non-null array
	    Object[] listeners = listenerList.getListenerList();
	    // Process the listeners last to first, notifying
	    // those that are interested in this event
	    for (int i = listeners.length - 2; i &gt;= 0; i -= 2) {
		if (listeners[i] == DocumentListener.class) {
		    // Lazily create the event:
		    // if (e == null)
		    // e = new ListSelectionEvent(this, firstIndex, lastIndex);
		    ((DocumentListener) listeners[i + 1]).changedUpdate(e);
		}
	    }
	} finally {
	    notifyingListeners = false;
	}
    }

    /**
     * True will notifying listeners.
     */
    private transient boolean notifyingListeners;
    /**
     * The event listener list for the document.
     */
    protected EventListenerList listenerList = new EventListenerList();

}

