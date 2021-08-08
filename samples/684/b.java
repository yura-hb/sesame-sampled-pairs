import java.awt.event.*;

class AWTEventMonitor {
    class AWTEventsListener implements TopLevelWindowListener, ActionListener, AdjustmentListener, ComponentListener,
	    ContainerListener, FocusListener, ItemListener, KeyListener, MouseListener, MouseMotionListener,
	    TextListener, WindowListener, ChangeListener {
	/**
	 * Called when the mouse is clicked.
	 *
	 * @see AWTEventMonitor#addMouseListener
	 */
	public void mouseClicked(MouseEvent e) {
	    if (AWTEventMonitor.mouseListener_private != null) {
		AWTEventMonitor.mouseListener_private.mouseClicked(e);
	    }
	}

    }

    static private MouseListener mouseListener_private = null;

}

