import org.nd4j.linalg.api.ndarray.INDArray;

class QuadTree implements Serializable {
    /**
     * Returns the cell of this element
     *
     * @param coordinates
     * @return
     */
    protected QuadTree findIndex(INDArray coordinates) {

	// Compute the sector for the coordinates
	boolean left = (coordinates.getDouble(0) &lt;= (boundary.getX() + boundary.getHw() / 2));
	boolean top = (coordinates.getDouble(1) &lt;= (boundary.getY() + boundary.getHh() / 2));

	// top left
	QuadTree index = getNorthWest();
	if (left) {
	    // left side
	    if (!top) {
		// bottom left
		index = getSouthWest();
	    }
	} else {
	    // right side
	    if (top) {
		// top right
		index = getNorthEast();
	    } else {
		// bottom right
		index = getSouthEast();

	    }
	}

	return index;
    }

    private Cell boundary;
    private QuadTree parent, northWest, northEast, southWest, southEast;
    private QuadTree parent, northWest, northEast, southWest, southEast;
    private QuadTree parent, northWest, northEast, southWest, southEast;
    private QuadTree parent, northWest, northEast, southWest, southEast;

    public QuadTree getNorthWest() {
	return northWest;
    }

    public QuadTree getSouthWest() {
	return southWest;
    }

    public QuadTree getNorthEast() {
	return northEast;
    }

    public QuadTree getSouthEast() {
	return southEast;
    }

}

