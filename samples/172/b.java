class ArrayUtils {
    /**
     * &lt;p&gt;Reverses the order of the given array.
     *
     * &lt;p&gt;This method does nothing for a {@code null} input array.
     *
     * @param array  the array to reverse, may be {@code null}
     */
    public static void reverse(final float[] array) {
	if (array == null) {
	    return;
	}
	reverse(array, 0, array.length);
    }

    /**
     * &lt;p&gt;
     * Reverses the order of the given array in the given range.
     *
     * &lt;p&gt;
     * This method does nothing for a {@code null} input array.
     *
     * @param array
     *            the array to reverse, may be {@code null}
     * @param startIndexInclusive
     *            the starting index. Undervalue (&lt;0) is promoted to 0, overvalue (&gt;array.length) results in no
     *            change.
     * @param endIndexExclusive
     *            elements up to endIndex-1 are reversed in the array. Undervalue (&lt; start index) results in no
     *            change. Overvalue (&gt;array.length) is demoted to array length.
     * @since 3.2
     */
    public static void reverse(final float[] array, final int startIndexInclusive, final int endIndexExclusive) {
	if (array == null) {
	    return;
	}
	int i = startIndexInclusive &lt; 0 ? 0 : startIndexInclusive;
	int j = Math.min(array.length, endIndexExclusive) - 1;
	float tmp;
	while (j &gt; i) {
	    tmp = array[j];
	    array[j] = array[i];
	    array[i] = tmp;
	    j--;
	    i++;
	}
    }

}

