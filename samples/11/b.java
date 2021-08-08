class Lists {
    /**
    * Returns consecutive {@linkplain List#subList(int, int) sublists} of a list, each of the same
    * size (the final list may be smaller). For example, partitioning a list containing {@code [a, b,
    * c, d, e]} with a partition size of 3 yields {@code [[a, b, c], [d, e]]} -- an outer list
    * containing two inner lists of three and two elements, all in the original order.
    *
    * &lt;p&gt;The outer list is unmodifiable, but reflects the latest state of the source list. The inner
    * lists are sublist views of the original list, produced on demand using {@link List#subList(int,
    * int)}, and are subject to all the usual caveats about modification as explained in that API.
    *
    * @param list the list to return consecutive sublists of
    * @param size the desired size of each sublist (the last may be smaller)
    * @return a list of consecutive sublists
    * @throws IllegalArgumentException if {@code partitionSize} is nonpositive
    */
    public static &lt;T&gt; List&lt;List&lt;T&gt;&gt; partition(List&lt;T&gt; list, int size) {
	checkNotNull(list);
	checkArgument(size &gt; 0);
	return (list instanceof RandomAccess) ? new RandomAccessPartition&lt;&gt;(list, size) : new Partition&lt;&gt;(list, size);
    }

    class RandomAccessPartition&lt;T&gt; extends Partition&lt;T&gt; implements RandomAccess {
	RandomAccessPartition(List&lt;T&gt; list, int size) {
	    super(list, size);
	}

    }

    class Partition&lt;T&gt; extends AbstractList&lt;List&lt;T&gt;&gt; {
	Partition(List&lt;T&gt; list, int size) {
	    this.list = list;
	    this.size = size;
	}

    }

}

