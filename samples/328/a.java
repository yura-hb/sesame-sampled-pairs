import java.util.List;

class Cluster implements Serializable {
    /**
     * Remove the point and return it
     * @param id
     * @return
     */
    public Point removePoint(String id) {
	Point removePoint = null;
	for (Point point : points)
	    if (id.equals(point.getId()))
		removePoint = point;
	if (removePoint != null)
	    points.remove(removePoint);
	return removePoint;
    }

    private List&lt;Point&gt; points = Collections.synchronizedList(new ArrayList&lt;Point&gt;());

}

