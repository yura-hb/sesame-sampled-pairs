import java.util.List;
import org.apache.commons.math4.geometry.spherical.oned.Arc;
import org.apache.commons.math4.util.FastMath;
import org.apache.commons.math4.util.MathUtils;

class Edge {
    /** Split the edge.
     * &lt;p&gt;
     * Once split, this edge is not referenced anymore by the vertices,
     * it is replaced by the two or three sub-edges and intermediate splitting
     * vertices are introduced to connect these sub-edges together.
     * &lt;/p&gt;
     * @param splitCircle circle splitting the edge in several parts
     * @param outsideList list where to put parts that are outside of the split circle
     * @param insideList list where to put parts that are inside the split circle
     */
    void split(final Circle splitCircle, final List&lt;Edge&gt; outsideList, final List&lt;Edge&gt; insideList) {

	// get the inside arc, synchronizing its phase with the edge itself
	final double edgeStart = circle.getPhase(start.getLocation().getVector());
	final Arc arc = circle.getInsideArc(splitCircle);
	final double arcRelativeStart = PlaneAngleRadians.normalize(arc.getInf(), edgeStart + FastMath.PI) - edgeStart;
	final double arcRelativeEnd = arcRelativeStart + arc.getSize();
	final double unwrappedEnd = arcRelativeEnd - MathUtils.TWO_PI;

	// build the sub-edges
	final double tolerance = circle.getTolerance();
	Vertex previousVertex = start;
	if (unwrappedEnd &gt;= length - tolerance) {

	    // the edge is entirely contained inside the circle
	    // we don't split anything
	    insideList.add(this);

	} else {

	    // there are at least some parts of the edge that should be outside
	    // (even is they are later be filtered out as being too small)
	    double alreadyManagedLength = 0;
	    if (unwrappedEnd &gt;= 0) {
		// the start of the edge is inside the circle
		previousVertex = addSubEdge(previousVertex,
			new Vertex(new S2Point(circle.getPointAt(edgeStart + unwrappedEnd))), unwrappedEnd, insideList,
			splitCircle);
		alreadyManagedLength = unwrappedEnd;
	    }

	    if (arcRelativeStart &gt;= length - tolerance) {
		// the edge ends while still outside of the circle
		if (unwrappedEnd &gt;= 0) {
		    previousVertex = addSubEdge(previousVertex, end, length - alreadyManagedLength, outsideList,
			    splitCircle);
		} else {
		    // the edge is entirely outside of the circle
		    // we don't split anything
		    outsideList.add(this);
		}
	    } else {
		// the edge is long enough to enter inside the circle
		previousVertex = addSubEdge(previousVertex,
			new Vertex(new S2Point(circle.getPointAt(edgeStart + arcRelativeStart))),
			arcRelativeStart - alreadyManagedLength, outsideList, splitCircle);
		alreadyManagedLength = arcRelativeStart;

		if (arcRelativeEnd &gt;= length - tolerance) {
		    // the edge ends while still inside of the circle
		    previousVertex = addSubEdge(previousVertex, end, length - alreadyManagedLength, insideList,
			    splitCircle);
		} else {
		    // the edge is long enough to exit outside of the circle
		    previousVertex = addSubEdge(previousVertex,
			    new Vertex(new S2Point(circle.getPointAt(edgeStart + arcRelativeStart))),
			    arcRelativeStart - alreadyManagedLength, insideList, splitCircle);
		    alreadyManagedLength = arcRelativeStart;
		    previousVertex = addSubEdge(previousVertex, end, length - alreadyManagedLength, outsideList,
			    splitCircle);
		}
	    }

	}

    }

    /** Circle supporting the edge. */
    private final Circle circle;
    /** Start vertex. */
    private final Vertex start;
    /** Length of the arc. */
    private final double length;
    /** End vertex. */
    private Vertex end;

    /** Add a sub-edge to a list if long enough.
     * &lt;p&gt;
     * If the length of the sub-edge to add is smaller than the {@link Circle#getTolerance()}
     * tolerance of the support circle, it will be ignored.
     * &lt;/p&gt;
     * @param subStart start of the sub-edge
     * @param subEnd end of the sub-edge
     * @param subLength length of the sub-edge
     * @param splitCircle circle splitting the edge in several parts
     * @param list list where to put the sub-edge
     * @return end vertex of the edge ({@code subEnd} if the edge was long enough and really
     * added, {@code subStart} if the edge was too small and therefore ignored)
     */
    private Vertex addSubEdge(final Vertex subStart, final Vertex subEnd, final double subLength, final List&lt;Edge&gt; list,
	    final Circle splitCircle) {

	if (subLength &lt;= circle.getTolerance()) {
	    // the edge is too short, we ignore it
	    return subStart;
	}

	// really add the edge
	subEnd.bindWith(splitCircle);
	final Edge edge = new Edge(subStart, subEnd, subLength, circle);
	list.add(edge);
	return subEnd;

    }

    /** Build an edge not contained in any node yet.
     * @param start start vertex
     * @param end end vertex
     * @param length length of the arc (it can be greater than \( \pi \))
     * @param circle circle supporting the edge
     */
    Edge(final Vertex start, final Vertex end, final double length, final Circle circle) {

	this.start = start;
	this.end = end;
	this.length = length;
	this.circle = circle;

	// connect the vertices back to the edge
	start.setOutgoing(this);
	end.setIncoming(this);

    }

}

