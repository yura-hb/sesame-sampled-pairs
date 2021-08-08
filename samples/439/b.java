import javax.swing.event.*;

class JTextField extends JTextComponent implements SwingConstants {
    /**
     * Returns true if the receiver has an &lt;code&gt;ActionListener&lt;/code&gt;
     * installed.
     */
    boolean hasActionListener() {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length - 2; i &gt;= 0; i -= 2) {
	    if (listeners[i] == ActionListener.class) {
		return true;
	    }
	}
	return false;
    }

}

