class Cluster implements Serializable {
    /**
     * Return the point with the given id
     * @param id
     * @return
     */
    public Point getPoint(String id) {
	for (Point point : points)
	    if (id.equals(point.getId()))
		return point;
	return null;
    }

    private List&lt;Point&gt; points = Collections.synchronizedList(new ArrayList&lt;Point&gt;());

}

