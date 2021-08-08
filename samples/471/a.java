import java.awt.Point;
import javax.swing.JComponent;

class NodeView extends JComponent implements TreeModelListener {
    /**
     * Returns the Point where the Links should arrive the Node.
     */
    public Point getLinkPoint(Point declination) {
	int x, y;
	Point linkPoint;
	if (declination != null) {
	    x = getMap().getZoomed(declination.x);
	    y = getMap().getZoomed(declination.y);
	} else {
	    x = 1;
	    y = 0;
	}
	if (isLeft()) {
	    x = -x;
	}
	if (y != 0) {
	    double ctgRect = Math.abs((double) getContent().getWidth() / getContent().getHeight());
	    double ctgLine = Math.abs((double) x / y);
	    int absLinkX, absLinkY;
	    if (ctgRect &gt; ctgLine) {
		absLinkX = Math.abs(x * getContent().getHeight() / (2 * y));
		absLinkY = getContent().getHeight() / 2;
	    } else {
		absLinkX = getContent().getWidth() / 2;
		absLinkY = Math.abs(y * getContent().getWidth() / (2 * x));
	    }
	    linkPoint = new Point(getContent().getWidth() / 2 + (x &gt; 0 ? absLinkX : -absLinkX),
		    getContent().getHeight() / 2 + (y &gt; 0 ? absLinkY : -absLinkY));
	} else {
	    linkPoint = new Point((x &gt; 0 ? getContent().getWidth() : 0), (getContent().getHeight() / 2));
	}
	linkPoint.translate(getContent().getX(), getContent().getY());
	convertPointToMap(linkPoint);
	return linkPoint;
    }

    protected MapView mapView;
    private JComponent contentPane;
    private MainView mainView;
    protected MindMapNode model;

    public MapView getMap() {
	return mapView;
    }

    /** Is the node left of root? */
    public boolean isLeft() {
	return getModel().isLeft();
    }

    public JComponent getContent() {
	return contentPane == null ? mainView : contentPane;
    }

    protected Point convertPointToMap(Point p) {
	return Tools.convertPointToAncestor(this, p, getMap());
    }

    public MindMapNode getModel() {
	return model;
    }

}

