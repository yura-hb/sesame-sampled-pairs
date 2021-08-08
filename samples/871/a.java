import java.util.Random;

class ArrayUtils {
    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array   the array to shuffle
     * @param random  the source of randomness used to permute the elements
     * @see &lt;a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle"&gt;Fisher-Yates shuffle algorithm&lt;/a&gt;
     * @since 3.6
     */
    public static void shuffle(final byte[] array, final Random random) {
	for (int i = array.length; i &gt; 1; i--) {
	    swap(array, i - 1, random.nextInt(i), 1);
	}
    }

    /**
     * Swaps a series of elements in the given byte array.
     *
     * &lt;p&gt;This method does nothing for a {@code null} or empty input array or
     * for overflow indices. Negative indices are promoted to 0(zero). If any
     * of the sub-arrays to swap falls outside of the given array, then the
     * swap is stopped at the end of the array and as many as possible elements
     * are swapped.&lt;/p&gt;
     *
     * Examples:
     * &lt;ul&gt;
     *     &lt;li&gt;ArrayUtils.swap([1, 2, 3, 4], 0, 2, 1) -&gt; [3, 2, 1, 4]&lt;/li&gt;
     *     &lt;li&gt;ArrayUtils.swap([1, 2, 3, 4], 0, 0, 1) -&gt; [1, 2, 3, 4]&lt;/li&gt;
     *     &lt;li&gt;ArrayUtils.swap([1, 2, 3, 4], 2, 0, 2) -&gt; [3, 4, 1, 2]&lt;/li&gt;
     *     &lt;li&gt;ArrayUtils.swap([1, 2, 3, 4], -3, 2, 2) -&gt; [3, 4, 1, 2]&lt;/li&gt;
     *     &lt;li&gt;ArrayUtils.swap([1, 2, 3, 4], 0, 3, 3) -&gt; [4, 2, 3, 1]&lt;/li&gt;
     * &lt;/ul&gt;
     *
     * @param array the array to swap, may be {@code null}
     * @param offset1 the index of the first element in the series to swap
     * @param offset2 the index of the second element in the series to swap
     * @param len the number of elements to swap starting with the given indices
     * @since 3.5
     */
    public static void swap(final byte[] array, int offset1, int offset2, int len) {
	if (array == null || array.length == 0 || offset1 &gt;= array.length || offset2 &gt;= array.length) {
	    return;
	}
	if (offset1 &lt; 0) {
	    offset1 = 0;
	}
	if (offset2 &lt; 0) {
	    offset2 = 0;
	}
	len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
	for (int i = 0; i &lt; len; i++, offset1++, offset2++) {
	    final byte aux = array[offset1];
	    array[offset1] = array[offset2];
	    array[offset2] = aux;
	}
    }

}

