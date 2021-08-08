import java.awt.*;

class BasicTreeUI extends TreeUI {
    class MouseInputHandler implements MouseInputListener {
	/**
	 * Removes an event from the source.
	 */
	protected void removeFromSource() {
	    if (source != null) {
		source.removeMouseListener(this);
		source.removeMouseMotionListener(this);
		if (focusComponent != null && focusComponent == destination && !dispatchedEvent
			&& (focusComponent instanceof JTextField)) {
		    ((JTextField) focusComponent).selectAll();
		}
	    }
	    source = destination = null;
	}

	/** Source that events are coming from. */
	protected Component source;
	private Component focusComponent;
	/** Destination that receives all events. */
	protected Component destination;
	private boolean dispatchedEvent;

    }

}

