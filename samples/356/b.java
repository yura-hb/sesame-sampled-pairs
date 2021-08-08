import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.shape.Shape;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.*;
import org.nd4j.linalg.util.ArrayUtil;
import org.nd4j.linalg.util.LongUtils;
import java.util.*;
import static org.nd4j.linalg.factory.Nd4j.*;

abstract class BaseNDArray implements INDArray, Iterable {
    /**
     * Get the specified column
     *
     * @param c
     */
    @Override
    public INDArray getColumn(long c) {
	Nd4j.getCompressor().autoDecompress(this);

	if (isColumnVector() && c == 0)
	    return this;
	else if (isColumnVector() && c &gt; 0)
	    throw new IllegalArgumentException("Illegal index for row");
	else if (isRowVector()) {
	    return Nd4j.scalar(getDouble(c));
	}
	return get(NDArrayIndex.all(), NDArrayIndex.point(c));
    }

    protected transient JvmShapeInfo jvmShapeInfo;
    protected transient volatile DataBuffer data;
    protected transient volatile DataBuffer shapeInformation;

    /**
     * Checks whether the matrix is a column vector.
     */
    @Override
    public boolean isColumnVector() {
	return rank() == 2 && columns() == 1 && length() &gt; 1;
    }

    /**
     * Checks whether the matrix is a row vector.
     */
    @Override
    public boolean isRowVector() {
	return (rank() == 2 && rows() == 1) && length() &gt; 1 || rank() == 1 && length() &gt; 1;
    }

    @Override
    public double getDouble(long i) {
	Nd4j.getCompressor().autoDecompress(this);

	if (i &gt;= length()) {
	    throw new IllegalArgumentException(
		    "Unable to get linear index " + i + ": values is greater than length (" + length() + ")");
	}

	autoProcessScalarCall();

	if (i == 0)
	    return data().getDouble(i);

	long[] dimensions = ordering() == 'c' ? Shape.ind2subC(this, i) : Shape.ind2sub(this, i);
	Shape.assertShapeLessThan(dimensions, shape());
	return getDouble(dimensions);

    }

    /**
     * Returns a subset of this array based on the specified
     * indexes
     *
     * @param indexes the indexes in to the array
     * @return a view of the array with the specified indices
     */
    @Override
    public INDArray get(INDArrayIndex... indexes) {
	Nd4j.getCompressor().autoDecompress(this);
	if (indexes.length &gt; rank()) {
	    int numNonNewAxis = 0;
	    for (int i = 0; i &lt; indexes.length; i++) {
		if (!(indexes[i] instanceof NewAxis))
		    numNonNewAxis++;
	    }

	    if (numNonNewAxis &gt; rank()) {
		throw new IllegalArgumentException("Too many indices for array. Number of indexes must be &lt;= rank()");
	    }
	}

	//check for row/column vector and point index being 0
	if (indexes.length == 1 && indexes[0] instanceof NDArrayIndexAll || (indexes.length == 2 && (isRowVector()
		&& indexes[0] instanceof PointIndex && indexes[0].offset() == 0 && indexes[1] instanceof NDArrayIndexAll
		|| isColumnVector() && indexes[1] instanceof PointIndex && indexes[0].offset() == 0
			&& indexes[0] instanceof NDArrayIndexAll)))
	    return this;

	indexes = NDArrayIndex.resolve(shapeInfoDataBuffer(), indexes);
	ShapeOffsetResolution resolution = new ShapeOffsetResolution(this);
	resolution.exec(indexes);

	if (indexes.length &lt; 1)
	    throw new IllegalStateException("Invalid index found of zero length");

	// FIXME: LONG
	int[] shape = LongUtils.toInts(resolution.getShapes());
	int numSpecifiedIndex = 0;
	for (int i = 0; i &lt; indexes.length; i++)
	    if (indexes[i] instanceof SpecifiedIndex)
		numSpecifiedIndex++;

	if (shape != null && numSpecifiedIndex &gt; 0) {
	    Generator&lt;List&lt;List&lt;Long&gt;&gt;&gt; gen = SpecifiedIndex.iterate(indexes);
	    INDArray ret = Nd4j.create(shape, 'c');
	    int count = 0;
	    while (true) {
		try {
		    List&lt;List&lt;Long&gt;&gt; next = gen.next();
		    List&lt;Long&gt; coordsCombo = new ArrayList&lt;&gt;();
		    for (int i = 0; i &lt; next.size(); i++) {
			if (next.get(i).size() &gt; 1)
			    throw new IllegalStateException("Illegal entry returned");
			coordsCombo.add(next.get(i).get(0));
		    }
		    ret.putScalar(count++, getDouble(Ints.toArray(coordsCombo)));

		} catch (NoSuchElementException e) {
		    break;
		}

		if (count &gt;= ret.length())
		    break;
	    }

	    return ret;

	}

	INDArray ret = subArray(resolution);
	return ret;
    }

    @Override
    public int rank() {
	return jvmShapeInfo.rank;
    }

    /**
     * Number of columns (shape[1]), throws an exception when
     * called when not 2d
     *
     * @return the number of columns in the array (only 2d)
     */
    @Override
    public int columns() {
	// FIXME: int cast
	if (isMatrix())
	    return (int) size(1);
	else if (Shape.isColumnVectorShape(shape())) {
	    return 1;
	} else if (Shape.isRowVectorShape(shape())) {
	    return (int) length();
	}
	throw new IllegalStateException("Rank is [" + rank() + "]; columns() call is not valid");

    }

    /**
     * Returns the total number of elements in the ndarray
     *
     * @return the number of elements in the ndarray
     */
    @Override
    public long length() {
	return jvmShapeInfo.length;
    }

    /**
     * Returns the number of rows
     * in the array (only 2d) throws an exception when
     * called when not 2d
     *
     * @return the number of rows in the matrix
     */
    @Override
    public int rows() {
	// FIXME:
	if (isMatrix())
	    return (int) size(0);
	else if (Shape.isRowVectorShape(shape())) {
	    return 1;
	} else if (Shape.isColumnVectorShape(shape())) {
	    return (int) length();
	}

	throw new IllegalStateException("Rank is " + rank() + " rows() call is not valid");
    }

    protected void autoProcessScalarCall() {
	/* if (Nd4j.getExecutioner().getProfilingMode() != OpExecutioner.ProfilingMode.DISABLED && Nd4j.getExecutioner().getProfilingMode() != OpExecutioner.ProfilingMode.SCOPE_PANIC)
	    OpProfiler.getInstance().processScalarCall();*/
    }

    @Override
    public DataBuffer data() {
	return data;
    }

    @Override
    public char ordering() {
	return jvmShapeInfo.order;
    }

    /**
     * Returns the shape(dimensions) of this array
     *
     * @return the shape of this matrix
     */
    public long[] shape() {
	return jvmShapeInfo.shape;
    }

    @Override
    public double getDouble(long... indices) {
	autoProcessScalarCall();
	Nd4j.getCompressor().autoDecompress(this);

	for (int i = 0; i &lt; indices.length; i++) {
	    if (indices[i] &lt; 0)
		indices[i] += rank();
	}
	if (indices.length == 1) {
	    if (rank() == 1)
		return Shape.getDouble(this, indices[0]);
	    else if (isRowVector())
		return Shape.getDouble(this, 0, indices[0]);
	    else if (isColumnVector())
		return Shape.getDouble(this, indices[0], 0);
	    else if (isScalar() && indices[0] == 0)
		return data().getDouble(0);
	    else
		throw new IllegalStateException("Indexes length must be &gt; 1 for non vectors and scalars");
	}
	return Shape.getDouble(this, indices);
    }

    @Override
    public DataBuffer shapeInfoDataBuffer() {
	return shapeInformation;
    }

    /**
     * Returns the elements at the the specified indices
     *
     * @param indices the indices to get
     * @return the array with the specified elements
     */
    @Override
    public double getDouble(int... indices) {
	autoProcessScalarCall();
	Nd4j.getCompressor().autoDecompress(this);

	for (int i = 0; i &lt; indices.length; i++) {
	    if (indices[i] &lt; 0)
		indices[i] += rank();
	}
	if (indices.length == 1) {
	    if (rank() == 1)
		return Shape.getDouble(this, indices[0]);
	    else if (isRowVector())
		return Shape.getDouble(this, 0, indices[0]);
	    else if (isColumnVector())
		return Shape.getDouble(this, indices[0], 0);
	    else if ((isScalar() || length() == 1) && indices[0] == 0)
		return data().getDouble(0);
	}
	return Shape.getDouble(this, indices);
    }

    @Override
    public INDArray subArray(ShapeOffsetResolution resolution) {
	Nd4j.getCompressor().autoDecompress(this);
	long[] offsets = resolution.getOffsets();
	int[] shape = LongUtils.toInts(resolution.getShapes());
	int[] stride = LongUtils.toInts(resolution.getStrides());

	//        if (offset() + resolution.getOffset() &gt;= Integer.MAX_VALUE)
	//            throw new IllegalArgumentException("Offset of array can not be &gt;= Integer.MAX_VALUE");

	long offset = (offset() + resolution.getOffset());

	int n = shape.length;

	// FIXME: shapeInfo should be used here
	if (shape.length &lt; 1)
	    return create(Nd4j.createBufferDetached(shape));
	if (offsets.length != n)
	    throw new IllegalArgumentException("Invalid offset " + Arrays.toString(offsets));
	if (stride.length != n)
	    throw new IllegalArgumentException("Invalid stride " + Arrays.toString(stride));

	if (shape.length == rank() && Shape.contentEquals(shape, shapeOf())) {
	    if (ArrayUtil.isZero(offsets)) {
		return this;
	    } else {
		throw new IllegalArgumentException("Invalid subArray offsets");
	    }
	}

	char newOrder = Shape.getOrder(shape, stride, 1);

	return create(data, Arrays.copyOf(shape, shape.length), stride, offset, newOrder);
    }

    /**
     * Returns true if this ndarray is 2d
     * or 3d with a singleton element
     *
     * @return true if the element is a matrix, false otherwise
     */
    public boolean isMatrix() {
	int rank = rank();
	return (rank == 2 && (size(0) != 1 && size(1) != 1));
    }

    /**
     * Returns the size of this array
     * along a particular dimension
     *
     * @param dimension the dimension to return from
     * @return the shape of the specified dimension
     */
    @Override
    public long size(int dimension) {
	if (dimension &lt; 0)
	    dimension += jvmShapeInfo.rank;

	if (isScalar()) {
	    if (dimension == 0 || dimension == 1 || dimension &lt; 0)
		return length();
	    else
		throw new IllegalArgumentException("Illegal dimension for scalar " + dimension);
	}

	if (dimension &gt;= rank())
	    throw new IllegalArgumentException("Invalid size: cannot get size of dimension " + dimension + " for rank "
		    + rank() + " NDArray (array shape: " + Arrays.toString(this.shape()) + ")");

	return jvmShapeInfo.shape[dimension];
    }

    /**
     * Test whether a matrix is scalar.
     */
    @Override
    public boolean isScalar() {
	if (isEmpty())
	    return false;

	if (jvmShapeInfo.rank == 0) {
	    return true;
	} else if (jvmShapeInfo.rank &gt; 2) {
	    return false;
	} else if (jvmShapeInfo.rank == 1) {
	    return shape()[0] == 1;
	} else if (jvmShapeInfo.rank == 2) {
	    return shape()[0] == 1 && shape()[1] == 1 || length() == 1;
	}

	else
	    return false;

    }

    @Override
    public long offset() {
	if (data().offset() &gt;= Integer.MAX_VALUE)
	    throw new IllegalArgumentException("Offset of buffer can not be &gt;= Integer.MAX_VALUE");
	//  return Shape.offset(shapeInfo());
	return data().offset();
    }

    protected INDArray create(DataBuffer buffer) {
	return Nd4j.create(buffer);
    }

    protected DataBuffer shapeOf() {
	//        if (shape == null)
	//            shape = Shape.shapeOf(shapeInfoDataBuffer());
	//        return shape;

	return Shape.shapeOf(shapeInfoDataBuffer());
    }

    protected INDArray create(DataBuffer data, int[] newShape, int[] newStrides, long offset, char ordering) {
	return Nd4j.create(data, newShape, newStrides, offset, ordering);
    }

    /**
     * This method returns true if this INDArray is special case: no-value INDArray
     *
     * @return
     */
    @Override
    public boolean isEmpty() {
	return Shape.isEmpty(jvmShapeInfo.javaShapeInformation);
    }

}

