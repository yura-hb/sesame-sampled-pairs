import java.util.Vector;
import javax.swing.event.*;

class DefaultStyledDocument extends AbstractDocument implements StyledDocument {
    /**
     * Removes a document listener.
     *
     * @param listener the listener
     * @see Document#removeDocumentListener
     */
    public void removeDocumentListener(DocumentListener listener) {
	synchronized (listeningStyles) {
	    super.removeDocumentListener(listener);
	    if (listenerList.getListenerCount(DocumentListener.class) == 0) {
		for (int counter = listeningStyles.size() - 1; counter &gt;= 0; counter--) {
		    listeningStyles.elementAt(counter).removeChangeListener(styleChangeListener);
		}
		listeningStyles.removeAllElements();
		if (styleContextChangeListener != null) {
		    StyleContext styles = (StyleContext) getAttributeContext();
		    styles.removeChangeListener(styleContextChangeListener);
		}
	    }
	}
    }

    /** Styles listening to. */
    private transient Vector&lt;Style&gt; listeningStyles;
    /** Listens to Styles. */
    private transient ChangeListener styleChangeListener;
    /** Listens to Styles. */
    private transient ChangeListener styleContextChangeListener;

}

