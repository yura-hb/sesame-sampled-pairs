class ResizableDoubleArray implements DoubleArray, Serializable {
    /**
     * Adds an element to the end of this expandable array.
     *
     * @param value Value to be added to end of array.
     */
    @Override
    public void addElement(final double value) {
	if (internalArray.length &lt;= startIndex + numElements) {
	    expand();
	}
	internalArray[startIndex + numElements++] = value;
    }

    /**
     * The internal storage array.
     */
    private double[] internalArray;
    /**
     * The position of the first addressable element in the internal storage
     * array.  The addressable elements in the array are
     * {@code internalArray[startIndex],...,internalArray[startIndex + numElements - 1]}.
     */
    private int startIndex = 0;
    /**
     * The number of addressable elements in the array.  Note that this
     * has nothing to do with the length of the internal storage array.
     */
    private int numElements = 0;
    /**
     * Determines whether array expansion by {@code expansionFactor}
     * is additive or multiplicative.
     */
    private final ExpansionMode expansionMode;
    /**
     * The expansion factor of the array.  When the array needs to be expanded,
     * the new array size will be {@code internalArray.length * expansionFactor}
     * if {@code expansionMode} is set to MULTIPLICATIVE, or
     * {@code internalArray.length + expansionFactor} if
     * {@code expansionMode} is set to ADDITIVE.
     */
    private final double expansionFactor;

    /**
     * Expands the internal storage array using the expansion factor.
     * &lt;p&gt;
     * If {@code expansionMode} is set to MULTIPLICATIVE,
     * the new array size will be {@code internalArray.length * expansionFactor}.
     * If {@code expansionMode} is set to ADDITIVE, the length
     * after expansion will be {@code internalArray.length + expansionFactor}.
     */
    protected void expand() {
	// notice the use of FastMath.ceil(), this guarantees that we will always
	// have an array of at least currentSize + 1.   Assume that the
	// current initial capacity is 1 and the expansion factor
	// is 1.000000000000000001.  The newly calculated size will be
	// rounded up to 2 after the multiplication is performed.
	int newSize = 0;
	if (expansionMode == ExpansionMode.MULTIPLICATIVE) {
	    newSize = (int) FastMath.ceil(internalArray.length * expansionFactor);
	} else {
	    newSize = (int) (internalArray.length + FastMath.round(expansionFactor));
	}
	final double[] tempArray = new double[newSize];

	// Copy and swap
	System.arraycopy(internalArray, 0, tempArray, 0, internalArray.length);
	internalArray = tempArray;
    }

}

