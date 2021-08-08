import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

class MindMapNodeMotionListener extends NodeMotionAdapter {
    /** Invoked when a mouse button is pressed on a component and then dragged. */
    public void mouseDragged(MouseEvent e) {
	logger.fine("Event: mouseDragged");
	if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == (InputEvent.BUTTON1_DOWN_MASK)) {
	    final NodeMotionListenerView motionListenerView = (NodeMotionListenerView) e.getSource();
	    final NodeView nodeView = getNodeView(e);
	    final MapView mapView = nodeView.getMap();
	    MindMapNode node = nodeView.getModel();
	    Point point = e.getPoint();
	    Tools.convertPointToAncestor(motionListenerView, point, JScrollPane.class);
	    if (!isActive()) {
		setDragStartingPoint(point, node);
	    } else {
		Point dragNextPoint = point;
		if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 0) {
		    int nodeShiftY = getNodeShiftY(dragNextPoint, node, dragStartingPoint);
		    int hGap = getHGap(dragNextPoint, node, dragStartingPoint);
		    node.setShiftY(nodeShiftY);
		    node.setHGap(hGap);
		} else {
		    MindMapNode parentNode = nodeView.getVisibleParentView().getModel();
		    parentNode.setVGap(getVGap(dragNextPoint, dragStartingPoint));
		    c.getModeController().nodeRefresh(parentNode);
		}
		dragStartingPoint = dragNextPoint;
		c.getModeController().nodeRefresh(node);
	    }
	    Point mapPoint = e.getPoint();
	    Tools.convertPointToAncestor(motionListenerView, mapPoint, mapView);
	    boolean isEventPointVisible = mapView.getVisibleRect().contains(mapPoint);
	    if (!isEventPointVisible) {
		Rectangle r = new Rectangle(mapPoint);
		Rectangle bounds = mapView.getBounds();
		mapView.scrollRectToVisible(r);
		Rectangle bounds2 = mapView.getBounds();
		int diffx = bounds2.x - bounds.x;
		int diffy = bounds2.y - bounds.y;
		try {
		    mapPoint.translate(diffx, diffy);
		    // here, there are strange cases, when the mouse moves away.
		    // Workaround.
		    if (mapView.getVisibleRect().contains(mapPoint)) {
			(new Robot()).mouseMove(e.getXOnScreen() + diffx, e.getYOnScreen() + diffy);
		    }
		} catch (AWTException e1) {
		    freemind.main.Resources.getInstance().logException(e1);
		}
		dragStartingPoint.x += ((node.getHGap() &lt; 0) ? 2 : 1) * diffx;
		dragStartingPoint.y += ((node.getShiftY() &lt; 0) ? 2 : 1) * diffy;
	    }
	}
    }

    private static java.util.logging.Logger logger;
    private Point dragStartingPoint = null;
    private final MindMapController c;
    private int originalParentVGap;
    private int originalHGap;
    private int originalShiftY;

    /**
     */
    private NodeView getNodeView(MouseEvent e) {
	return ((NodeMotionListenerView) e.getSource()).getMovedView();
    }

    public boolean isActive() {
	return getDragStartingPoint() != null;
    }

    void setDragStartingPoint(Point point, MindMapNode node) {
	dragStartingPoint = point;
	if (point != null) {
	    originalParentVGap = node.getParentNode().getVGap();
	    originalHGap = node.getHGap();
	    originalShiftY = node.getShiftY();
	} else {
	    originalParentVGap = originalHGap = originalShiftY = 0;
	}
    }

    private int getNodeShiftY(Point dragNextPoint, MindMapNode pNode, Point dragStartingPoint) {
	int shiftY = pNode.getShiftY();
	int shiftYChange = (int) ((dragNextPoint.y - dragStartingPoint.y) / c.getView().getZoom());
	shiftY += shiftYChange;
	return shiftY;
    }

    private int getHGap(Point dragNextPoint, MindMapNode node, Point dragStartingPoint) {
	int oldHGap = node.getHGap();
	int hGapChange = (int) ((dragNextPoint.x - dragStartingPoint.x) / c.getView().getZoom());
	if (node.isLeft())
	    hGapChange = -hGapChange;
	oldHGap += +hGapChange;
	return oldHGap;
    }

    private int getVGap(Point dragNextPoint, Point dragStartingPoint) {
	int oldVGap = originalParentVGap;
	int vGapChange = (int) ((dragNextPoint.y - dragStartingPoint.y) / c.getView().getZoom());
	oldVGap = Math.max(0, oldVGap - vGapChange);
	return oldVGap;
    }

    Point getDragStartingPoint() {
	return dragStartingPoint;
    }

}

