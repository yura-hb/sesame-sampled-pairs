import java.util.StringTokenizer;
import org.apache.commons.math4.geometry.euclidean.threed.Cartesian3D;
import org.apache.commons.math4.geometry.spherical.twod.Circle;
import org.apache.commons.math4.geometry.spherical.twod.SphericalPolygonsSet;

class RegionParser {
    /** Parse a string representation of a {@link SphericalPolygonsSet}.
     * @param s string to parse
     * @return parsed region
     * @exception IOException if the string cannot be read
     * @exception ParseException if the string cannot be parsed
     */
    public static SphericalPolygonsSet parseSphericalPolygonsSet(final String s) throws IOException, ParseException {
	final TreeBuilder&lt;Sphere2D&gt; builder = new TreeBuilder&lt;Sphere2D&gt;("SphericalPolygonsSet", s) {

	    /** {@inheritDoc} */
	    @Override
	    public Circle parseHyperplane() throws IOException, ParseException {
		return new Circle(new Cartesian3D(getNumber(), getNumber(), getNumber()), getNumber());
	    }

	};
	return new SphericalPolygonsSet(builder.getTree(), builder.getTolerance());
    }

    abstract class TreeBuilder&lt;S&gt; {
	/** Get next number.
	 * @return parsed number
	 * @exception IOException if the string cannot be read
	 * @exception NumberFormatException if the string cannot be parsed
	 */
	protected double getNumber() throws IOException, NumberFormatException {
	    return Double.parseDouble(tokenizer.nextToken());
	}

	/** Get the built tree.
	 * @return built tree
	 */
	public BSPTree&lt;S&gt; getTree() {
	    return root;
	}

	/** Get the tolerance.
	 * @return tolerance
	 */
	public double getTolerance() {
	    return tolerance;
	}

    }

}

