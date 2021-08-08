import org.nd4j.base.Preconditions;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.shape.Shape;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.ArrayUtil;
import static org.nd4j.linalg.factory.Nd4j.*;

abstract class BaseNDArray implements INDArray, Iterable {
    /**
     * Return transposed copy of this matrix.
     */
    @Override
    public INDArray transpose() {
	return permute(ArrayUtil.reverseCopy(ArrayUtil.range(0, rank())));
    }

    protected transient JvmShapeInfo jvmShapeInfo;
    protected transient volatile DataBuffer data;
    protected transient volatile DataBuffer shapeInformation;

    @Override
    public int rank() {
	return jvmShapeInfo.rank;
    }

    /**
     * See: http://www.mathworks.com/help/matlab/ref/permute.html
     *
     * @param rearrange the dimensions to swap to
     * @return the newly permuted array
     */
    @Override
    public INDArray permute(int... rearrange) {
	Preconditions.checkArgument(rearrange.length == rank(),
		"Incorrect number of arguments for permute function:"
			+ " got arguments %s for rank %s array. Number of arguments must equal array rank",
		rearrange, rank());
	Nd4j.getCompressor().autoDecompress(this);
	boolean alreadyInOrder = true;
	//IntBuffer shapeInfo = shapeInfo();
	int rank = jvmShapeInfo.rank;
	for (int i = 0; i &lt; rank; i++) {
	    if (rearrange[i] != i) {
		alreadyInOrder = false;
		break;
	    }
	}

	if (alreadyInOrder)
	    return this;

	checkArrangeArray(rearrange);
	int[] newShape = doPermuteSwap(shapeOf(), rearrange);
	int[] newStride = doPermuteSwap(strideOf(), rearrange);

	char newOrder = Shape.getOrder(newShape, newStride, elementStride());

	INDArray value = create(data(), newShape, newStride, offset(), newOrder);
	return value;
    }

    protected void checkArrangeArray(int[] arr) {
	Preconditions.checkArgument(arr.length == jvmShapeInfo.rank,
		"Invalid rearrangement: number of arrangement (%s) != rank (%s)", arr.length, jvmShapeInfo.rank);
	for (int i = 0; i &lt; arr.length; i++) {
	    if (arr[i] &gt;= arr.length)
		throw new IllegalArgumentException("The specified dimensions can't be swapped. Given element " + i
			+ " was &gt;= number of dimensions");
	    if (arr[i] &lt; 0)
		throw new IllegalArgumentException("Invalid dimension: " + i + " : negative value");

	}

	for (int i = 0; i &lt; arr.length; i++) {
	    for (int j = 0; j &lt; arr.length; j++) {
		if (i != j && arr[i] == arr[j])
		    throw new IllegalArgumentException("Permute array must have unique elements");
	    }
	}

    }

    protected DataBuffer shapeOf() {
	//        if (shape == null)
	//            shape = Shape.shapeOf(shapeInfoDataBuffer());
	//        return shape;

	return Shape.shapeOf(shapeInfoDataBuffer());
    }

    protected int[] doPermuteSwap(DataBuffer shape, int[] rearrange) {
	int[] ret = new int[rearrange.length];
	for (int i = 0; i &lt; rearrange.length; i++) {
	    ret[i] = shape.getInt(rearrange[i]);
	}

	return ret;
    }

    protected DataBuffer strideOf() {
	//        if (stride == null)
	//            stride = Shape.stride(shapeInfoDataBuffer());
	//        return stride;
	return Shape.stride(shapeInfoDataBuffer());
    }

    @Override
    public int elementStride() {
	return 1;
    }

    @Override
    public DataBuffer data() {
	return data;
    }

    @Override
    public long offset() {
	if (data().offset() &gt;= Integer.MAX_VALUE)
	    throw new IllegalArgumentException("Offset of buffer can not be &gt;= Integer.MAX_VALUE");
	//  return Shape.offset(shapeInfo());
	return data().offset();
    }

    protected INDArray create(DataBuffer data, int[] newShape, int[] newStrides, long offset, char ordering) {
	return Nd4j.create(data, newShape, newStrides, offset, ordering);
    }

    @Override
    public DataBuffer shapeInfoDataBuffer() {
	return shapeInformation;
    }

}

