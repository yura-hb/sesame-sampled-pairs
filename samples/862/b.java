import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import sun.awt.dnd.SunDragSourceContextPeer;

class XMouseDragGestureRecognizer extends MouseDragGestureRecognizer {
    /**
     * Invoked when a mouse button is pressed on a component.
     */

    public void mouseDragged(MouseEvent e) {
	if (!events.isEmpty()) { // gesture pending
	    int dop = mapDragOperationFromModifiers(e);

	    if (dop == DnDConstants.ACTION_NONE) {
		return;
	    }

	    MouseEvent trigger = (MouseEvent) events.get(0);

	    Point origin = trigger.getPoint();
	    Point current = e.getPoint();

	    int dx = Math.abs(origin.x - current.x);
	    int dy = Math.abs(origin.y - current.y);

	    if (dx &gt; motionThreshold || dy &gt; motionThreshold) {
		fireDragGestureRecognized(dop, ((MouseEvent) getTriggerEvent()).getPoint());
	    } else
		appendEvent(e);
	}
    }

    protected static int motionThreshold;
    protected static final int ButtonMask = InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON2_DOWN_MASK
	    | InputEvent.BUTTON3_DOWN_MASK;

    /**
     * determine the drop action from the event
     */

    protected int mapDragOperationFromModifiers(MouseEvent e) {
	int mods = e.getModifiersEx();
	int btns = mods & ButtonMask;

	// Do not allow right mouse button drag since Motif DnD does not
	// terminate drag operation on right mouse button release.
	if (!(btns == InputEvent.BUTTON1_DOWN_MASK || btns == InputEvent.BUTTON2_DOWN_MASK)) {
	    return DnDConstants.ACTION_NONE;
	}

	return SunDragSourceContextPeer.convertModifiersToDropAction(mods, getSourceActions());
    }

}

