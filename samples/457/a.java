import org.nd4j.linalg.util.ArrayUtil;

abstract class BaseDataBuffer implements DataBuffer {
    /**
     * Special method for
     * @param i
     * @return
     */
    protected short getShort(long i) {
	if (dataType() != Type.HALF)
	    throw new UnsupportedOperationException("getShort() is supported for Half-precision buffers only");

	return fromFloat(((HalfIndexer) indexer).get(offset() + i));
    }

    protected transient Indexer indexer;
    protected Type type;
    protected long offset;

    /**
     * The data opType of the buffer
     *
     * @return the data opType of the buffer
     */
    @Override
    public Type dataType() {
	return type;
    }

    @Override
    public long offset() {
	return offset;
    }

    /**
     *
     * @param v
     * @return
     */
    public static short fromFloat(float v) {
	return ArrayUtil.fromFloat(v);
    }

}

