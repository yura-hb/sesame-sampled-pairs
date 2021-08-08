import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

class DropTarget implements DropTargetListener, Serializable {
    class DropTargetAutoScroller implements ActionListener {
	/**
	 * cause autoscroll to occur
	 *
	 * @param e the {@code ActionEvent}
	 */

	public synchronized void actionPerformed(ActionEvent e) {
	    updateRegion();

	    if (outer.contains(locn) && !inner.contains(locn))
		autoScroll.autoscroll(locn);
	}

	private Rectangle outer = new Rectangle();
	private Point locn;
	private Rectangle inner = new Rectangle();
	private Autoscroll autoScroll;
	private Component component;

	/**
	 * update the geometry of the autoscroll region
	 */

	@SuppressWarnings("deprecation")
	private void updateRegion() {
	    Insets i = autoScroll.getAutoscrollInsets();
	    Dimension size = component.getSize();

	    if (size.width != outer.width || size.height != outer.height)
		outer.reshape(0, 0, size.width, size.height);

	    if (inner.x != i.left || inner.y != i.top)
		inner.setLocation(i.left, i.top);

	    int newWidth = size.width - (i.left + i.right);
	    int newHeight = size.height - (i.top + i.bottom);

	    if (newWidth != inner.width || newHeight != inner.height)
		inner.setSize(newWidth, newHeight);

	}

    }

}

