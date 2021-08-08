class ListUtils {
    /**
     * Returns consecutive {@link List#subList(int, int) sublists} of a
     * list, each of the same size (the final list may be smaller). For example,
     * partitioning a list containing {@code [a, b, c, d, e]} with a partition
     * size of 3 yields {@code [[a, b, c], [d, e]]} -- an outer list containing
     * two inner lists of three and two elements, all in the original order.
     * &lt;p&gt;
     * The outer list is unmodifiable, but reflects the latest state of the
     * source list. The inner lists are sublist views of the original list,
     * produced on demand using {@link List#subList(int, int)}, and are subject
     * to all the usual caveats about modification as explained in that API.
     * &lt;p&gt;
     * Adapted from http://code.google.com/p/guava-libraries/
     *
     * @param &lt;T&gt; the element type
     * @param list  the list to return consecutive sublists of
     * @param size  the desired size of each sublist (the last may be smaller)
     * @return a list of consecutive sublists
     * @throws NullPointerException if list is null
     * @throws IllegalArgumentException if size is not strictly positive
     * @since 4.0
     */
    public static &lt;T&gt; List&lt;List&lt;T&gt;&gt; partition(final List&lt;T&gt; list, final int size) {
	if (list == null) {
	    throw new NullPointerException("List must not be null");
	}
	if (size &lt;= 0) {
	    throw new IllegalArgumentException("Size must be greater than 0");
	}
	return new Partition&lt;&gt;(list, size);
    }

    class Partition&lt;T&gt; extends AbstractList&lt;List&lt;T&gt;&gt; {
	private Partition(final List&lt;T&gt; list, final int size) {
	    this.list = list;
	    this.size = size;
	}

    }

}

