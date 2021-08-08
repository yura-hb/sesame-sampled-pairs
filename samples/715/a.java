import javax.swing.SwingUtilities;

class CommonNodeMouseMotionListener implements NodeMouseMotionObserver {
    class timeDelayedSelection extends TimerTask {
	/** TimerTask method to enable the selection after a given time. */
	public void run() {
	    /*
	     * formerly in ControllerAdapter. To guarantee, that point-to-select
	     * does not change selection if any meta key is pressed.
	     */
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    if (e.getModifiers() == 0 && !c.isBlocked() && c.getView().getSelecteds().size() &lt;= 1) {
			c.extendSelection(e);
		    }
		}
	    });
	}

	private final MouseEvent e;
	private final ModeController c;

    }

}

