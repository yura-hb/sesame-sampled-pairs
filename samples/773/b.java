class ImmutableDoubleArray implements Serializable {
    /** Returns an immutable array containing a single value. */
    public static ImmutableDoubleArray of(double e0) {
	return new ImmutableDoubleArray(new double[] { e0 });
    }

    @SuppressWarnings("Immutable")
    private final double[] array;
    private final transient int start;
    private final int end;

    private ImmutableDoubleArray(double[] array) {
	this(array, 0, array.length);
    }

    private ImmutableDoubleArray(double[] array, int start, int end) {
	this.array = array;
	this.start = start;
	this.end = end;
    }

}

