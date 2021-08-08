import java.awt.Component;

class MapView extends JPanel implements ViewAbstraction, Printable, Autoscroll {
    /**
     * Returns the size of the visible part of the view in view coordinates.
     */
    public Dimension getViewportSize() {
	if (getParent() instanceof JViewport) {
	    JViewport mapViewport = (JViewport) getParent();
	    return mapViewport == null ? null : mapViewport.getSize();
	}
	return null;
    }

}

