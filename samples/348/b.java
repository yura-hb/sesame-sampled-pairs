import java.awt.event.*;

class AWTEventMulticaster
	implements ComponentListener, ContainerListener, FocusListener, KeyListener, MouseListener, MouseMotionListener,
	WindowListener, WindowFocusListener, WindowStateListener, ActionListener, ItemListener, AdjustmentListener,
	TextListener, InputMethodListener, HierarchyListener, HierarchyBoundsListener, MouseWheelListener {
    /**
     * Handles the itemStateChanged event by invoking the
     * itemStateChanged methods on listener-a and listener-b.
     * @param e the item event
     */
    public void itemStateChanged(ItemEvent e) {
	((ItemListener) a).itemStateChanged(e);
	((ItemListener) b).itemStateChanged(e);
    }

    /**
     * A variable in the event chain (listener-a)
     */
    protected final EventListener a;
    /**
     * A variable in the event chain (listener-b)
     */
    protected final EventListener b;

}

