class TIFFTag {
    /**
     * Returns the number of bytes used to store a value of the given
     * data type.
     *
     * @param dataType the data type to be queried.
     *
     * @return the number of bytes used to store the given data type.
     *
     * @throws IllegalArgumentException if {@code datatype} is
     * less than {@code MIN_DATATYPE} or greater than
     * {@code MAX_DATATYPE}.
     */
    public static int getSizeOfType(int dataType) {
	if (dataType &lt; MIN_DATATYPE || dataType &gt; MAX_DATATYPE) {
	    throw new IllegalArgumentException("dataType out of range!");
	}

	return SIZE_OF_TYPE[dataType];
    }

    /**
     * The numerically smallest constant representing a TIFF data type.
     */
    public static final int MIN_DATATYPE = TIFF_BYTE;
    /**
     * The numerically largest constant representing a TIFF data type.
     */
    public static final int MAX_DATATYPE = TIFF_IFD_POINTER;
    private static final int[] SIZE_OF_TYPE = { 0, //  0 = n/a
	    1, //  1 = byte
	    1, //  2 = ascii
	    2, //  3 = short
	    4, //  4 = long
	    8, //  5 = rational
	    1, //  6 = sbyte
	    1, //  7 = undefined
	    2, //  8 = sshort
	    4, //  9 = slong
	    8, // 10 = srational
	    4, // 11 = float
	    8, // 12 = double
	    4, // 13 = IFD_POINTER
    };

}

