import java.util.Timer;

class CommonNodeMouseMotionListener implements NodeMouseMotionObserver {
    /** Invoked when a mouse button is pressed on a component and then dragged. */
    public void mouseDragged(MouseEvent e) {
	logger.fine("Event: mouseDragged");
	// first stop the timer and select the node:
	stopTimerForDelayedSelection();
	NodeView nodeV = ((MainView) e.getComponent()).getNodeView();

	// if dragged for the first time, select the node:
	if (!c.getView().isSelected(nodeV))
	    c.extendSelection(e);
    }

    private static java.util.logging.Logger logger;
    private final ModeController c;
    private Timer timerForDelayedSelection;
    /**
     * The mouse has to stay in this region to enable the selection after a
     * given time.
     */
    private Rectangle controlRegionForDelayedSelection;

    protected void stopTimerForDelayedSelection() {
	// stop timer.
	if (timerForDelayedSelection != null)
	    timerForDelayedSelection.cancel();
	timerForDelayedSelection = null;
	controlRegionForDelayedSelection = null;
    }

}

