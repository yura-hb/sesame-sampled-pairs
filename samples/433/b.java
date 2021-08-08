import java.awt.dnd.DnDConstants;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import sun.awt.dnd.SunDragSourceContextPeer;

class WMouseDragGestureRecognizer extends MouseDragGestureRecognizer {
    /**
     * Invoked when the mouse exits a component.
     */

    @Override
    public void mouseExited(MouseEvent e) {

	if (!events.isEmpty()) { // gesture pending
	    int dragAction = mapDragOperationFromModifiers(e);

	    if (dragAction == DnDConstants.ACTION_NONE) {
		events.clear();
	    }
	}
    }

    protected static final int ButtonMask = InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON2_DOWN_MASK
	    | InputEvent.BUTTON3_DOWN_MASK;

    /**
     * determine the drop action from the event
     */

    protected int mapDragOperationFromModifiers(MouseEvent e) {
	int mods = e.getModifiersEx();
	int btns = mods & ButtonMask;

	// Prohibit multi-button drags.
	if (!(btns == InputEvent.BUTTON1_DOWN_MASK || btns == InputEvent.BUTTON2_DOWN_MASK
		|| btns == InputEvent.BUTTON3_DOWN_MASK)) {
	    return DnDConstants.ACTION_NONE;
	}

	return SunDragSourceContextPeer.convertModifiersToDropAction(mods, getSourceActions());
    }

}

