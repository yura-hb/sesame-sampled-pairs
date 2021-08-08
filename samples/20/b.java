class ArrayUtils {
    /**
     * &lt;p&gt;Finds the index of the given object in the array starting at the given index.
     *
     * &lt;p&gt;This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
     *
     * &lt;p&gt;A negative startIndex is treated as zero. A startIndex larger than the array
     * length will return {@link #INDEX_NOT_FOUND} ({@code -1}).
     *
     * @param array  the array to search through for the object, may be {@code null}
     * @param objectToFind  the object to find, may be {@code null}
     * @param startIndex  the index to start searching at
     * @return the index of the object within the array starting at the index,
     *  {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
     */
    public static int indexOf(final Object[] array, final Object objectToFind, int startIndex) {
	if (array == null) {
	    return INDEX_NOT_FOUND;
	}
	if (startIndex &lt; 0) {
	    startIndex = 0;
	}
	if (objectToFind == null) {
	    for (int i = startIndex; i &lt; array.length; i++) {
		if (array[i] == null) {
		    return i;
		}
	    }
	} else {
	    for (int i = startIndex; i &lt; array.length; i++) {
		if (objectToFind.equals(array[i])) {
		    return i;
		}
	    }
	}
	return INDEX_NOT_FOUND;
    }

    /**
     * The index value when an element is not found in a list or array: {@code -1}.
     * This value is returned by methods in this class and can also be used in comparisons with values returned by
     * various method from {@link java.util.List}.
     */
    public static final int INDEX_NOT_FOUND = -1;

}

