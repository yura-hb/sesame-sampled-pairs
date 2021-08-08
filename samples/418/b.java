import java.awt.dnd.DnDConstants;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import sun.awt.dnd.SunDragSourceContextPeer;

class XMouseDragGestureRecognizer extends MouseDragGestureRecognizer {
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

    protected static final int ButtonMask = InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON2_DOWN_MASK
	    | InputEvent.BUTTON3_DOWN_MASK;

}

