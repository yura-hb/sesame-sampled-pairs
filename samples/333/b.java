import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

class BasicScrollPaneUI extends ScrollPaneUI implements ScrollPaneConstants {
    /**
     * Updates viewport.
     *
     * @param e the property change event
     */
    protected void updateViewport(PropertyChangeEvent e) {
	JViewport oldViewport = (JViewport) (e.getOldValue());
	JViewport newViewport = (JViewport) (e.getNewValue());

	if (oldViewport != null) {
	    oldViewport.removeChangeListener(viewportChangeListener);
	}

	if (newViewport != null) {
	    Point p = newViewport.getViewPosition();
	    if (scrollpane.getComponentOrientation().isLeftToRight()) {
		p.x = Math.max(p.x, 0);
	    } else {
		int max = newViewport.getViewSize().width;
		int extent = newViewport.getExtentSize().width;
		if (extent &gt; max) {
		    p.x = max - extent;
		} else {
		    p.x = Math.max(0, Math.min(max - extent, p.x));
		}
	    }
	    p.y = Math.max(p.y, 0);
	    newViewport.setViewPosition(p);
	    newViewport.addChangeListener(viewportChangeListener);
	}
    }

    /**
     * {@code ChangeListener} installed on the viewport.
     */
    protected ChangeListener viewportChangeListener;
    /**
     * The instance of {@code JScrollPane}.
     */
    protected JScrollPane scrollpane;

}

