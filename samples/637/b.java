import java.awt.*;
import javax.swing.event.*;

class JViewport extends JComponent implements Accessible {
    /**
     * Sets the size of the visible part of the view using view coordinates.
     *
     * @param newExtent  a &lt;code&gt;Dimension&lt;/code&gt; object specifying
     *          the size of the view
     */
    public void setExtentSize(Dimension newExtent) {
	Dimension oldExtent = getExtentSize();
	if (!newExtent.equals(oldExtent)) {
	    setSize(newExtent);
	    fireStateChanged();
	}
    }

    private transient ChangeEvent changeEvent = null;

    /**
     * Returns the size of the visible part of the view in view coordinates.
     *
     * @return a &lt;code&gt;Dimension&lt;/code&gt; object giving the size of the view
     */
    @Transient
    public Dimension getExtentSize() {
	return getSize();
    }

    /**
     * Notifies all &lt;code&gt;ChangeListeners&lt;/code&gt; when the views
     * size, position, or the viewports extent size has changed.
     *
     * @see #addChangeListener
     * @see #removeChangeListener
     * @see EventListenerList
     */
    protected void fireStateChanged() {
	Object[] listeners = listenerList.getListenerList();
	for (int i = listeners.length - 2; i &gt;= 0; i -= 2) {
	    if (listeners[i] == ChangeListener.class) {
		if (changeEvent == null) {
		    changeEvent = new ChangeEvent(this);
		}
		((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
	    }
	}
    }

}

