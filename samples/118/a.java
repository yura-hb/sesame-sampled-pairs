import java.util.Formatter;
import org.apache.commons.math4.geometry.euclidean.oned.OrientedPoint;
import org.apache.commons.math4.geometry.euclidean.oned.Cartesian1D;

class RegionDumper {
    /** Get a string representation of an {@link IntervalsSet}.
     * @param intervalsSet region to dump
     * @return string representation of the region
     */
    public static String dump(final IntervalsSet intervalsSet) {
	final TreeDumper&lt;Euclidean1D&gt; visitor = new TreeDumper&lt;Euclidean1D&gt;("IntervalsSet",
		intervalsSet.getTolerance()) {

	    /** {@inheritDoc} */
	    @Override
	    protected void formatHyperplane(final Hyperplane&lt;Euclidean1D&gt; hyperplane) {
		final OrientedPoint h = (OrientedPoint) hyperplane;
		getFormatter().format("%22.15e %b %22.15e", h.getLocation().getX(), h.isDirect(), h.getTolerance());
	    }

	};
	intervalsSet.getTree(false).visit(visitor);
	return visitor.getDump();
    }

    abstract class TreeDumper&lt;S&gt; implements BSPTreeVisitor&lt;S&gt; {
	/** Get the formatter to use.
	 * @return formatter to use
	 */
	protected Formatter getFormatter() {
	    return formatter;
	}

	/** Get the string representation of the tree.
	 * @return string representation of the tree.
	 */
	public String getDump() {
	    return dump.toString();
	}

    }

}

