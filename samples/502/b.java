import java.lang.reflect.Array;
import java.util.concurrent.ForkJoinPool;

class Arrays {
    /**
     * Sorts the specified array of objects into ascending order, according
     * to the {@linkplain Comparable natural ordering} of its elements.
     * All elements in the array must implement the {@link Comparable}
     * interface.  Furthermore, all elements in the array must be
     * &lt;i&gt;mutually comparable&lt;/i&gt; (that is, {@code e1.compareTo(e2)} must
     * not throw a {@code ClassCastException} for any elements {@code e1}
     * and {@code e2} in the array).
     *
     * &lt;p&gt;This sort is guaranteed to be &lt;i&gt;stable&lt;/i&gt;:  equal elements will
     * not be reordered as a result of the sort.
     *
     * @implNote The sorting algorithm is a parallel sort-merge that breaks the
     * array into sub-arrays that are themselves sorted and then merged. When
     * the sub-array length reaches a minimum granularity, the sub-array is
     * sorted using the appropriate {@link Arrays#sort(Object[]) Arrays.sort}
     * method. If the length of the specified array is less than the minimum
     * granularity, then it is sorted using the appropriate {@link
     * Arrays#sort(Object[]) Arrays.sort} method. The algorithm requires a
     * working space no greater than the size of the original array. The
     * {@link ForkJoinPool#commonPool() ForkJoin common pool} is used to
     * execute any parallel tasks.
     *
     * @param &lt;T&gt; the class of the objects to be sorted
     * @param a the array to be sorted
     *
     * @throws ClassCastException if the array contains elements that are not
     *         &lt;i&gt;mutually comparable&lt;/i&gt; (for example, strings and integers)
     * @throws IllegalArgumentException (optional) if the natural
     *         ordering of the array elements is found to violate the
     *         {@link Comparable} contract
     *
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static &lt;T extends Comparable&lt;? super T&gt;&gt; void parallelSort(T[] a) {
	int n = a.length, p, g;
	if (n &lt;= MIN_ARRAY_SORT_GRAN || (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
	    TimSort.sort(a, 0, n, NaturalOrder.INSTANCE, null, 0, 0);
	else
	    new ArraysParallelSortHelpers.FJObject.Sorter&lt;&gt;(null, a,
		    (T[]) Array.newInstance(a.getClass().getComponentType(), n), 0, n, 0,
		    ((g = n / (p &lt;&lt; 2)) &lt;= MIN_ARRAY_SORT_GRAN) ? MIN_ARRAY_SORT_GRAN : g, NaturalOrder.INSTANCE)
			    .invoke();
    }

    /**
     * The minimum array length below which a parallel sorting
     * algorithm will not further partition the sorting task. Using
     * smaller sizes typically results in memory contention across
     * tasks that makes parallel speedups unlikely.
     */
    private static final int MIN_ARRAY_SORT_GRAN = 1 &lt;&lt; 13;

}

