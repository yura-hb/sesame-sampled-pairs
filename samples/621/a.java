class ArrowConverter {
    /**
     *
     * @param allocator
     * @param name
     * @return
     */
    public static IntVector intVectorOf(BufferAllocator allocator, String name, int length) {
	IntVector float8Vector = new IntVector(name, FieldType.nullable(new ArrowType.Int(32, true)), allocator);
	float8Vector.allocateNew(length);

	float8Vector.setValueCount(length);

	return float8Vector;
    }

}

