class DoubleBuffer extends BaseDataBuffer {
    /**
     * Initialize the opType of this buffer
     */
    @Override
    protected void initTypeAndSize() {
	elementSize = 8;
	type = Type.DOUBLE;
    }

}

