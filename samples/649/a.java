import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.collections4.bag.HashBag;

class ListUtils {
    /**
     * Returns the sum of the given lists.  This is their intersection
     * subtracted from their union.
     *
     * @param &lt;E&gt; the element type
     * @param list1  the first list
     * @param list2  the second list
     * @return  a new list containing the sum of those lists
     * @throws NullPointerException if either list is null
     */
    public static &lt;E&gt; List&lt;E&gt; sum(final List&lt;? extends E&gt; list1, final List&lt;? extends E&gt; list2) {
	return subtract(union(list1, list2), intersection(list1, list2));
    }

    /**
     * Returns a new list containing the second list appended to the
     * first list.  The {@link List#addAll(Collection)} operation is
     * used to append the two given lists into a new list.
     *
     * @param &lt;E&gt; the element type
     * @param list1  the first list
     * @param list2  the second list
     * @return a new list containing the union of those lists
     * @throws NullPointerException if either list is null
     */
    public static &lt;E&gt; List&lt;E&gt; union(final List&lt;? extends E&gt; list1, final List&lt;? extends E&gt; list2) {
	final ArrayList&lt;E&gt; result = new ArrayList&lt;&gt;(list1.size() + list2.size());
	result.addAll(list1);
	result.addAll(list2);
	return result;
    }

    /**
     * Returns a new list containing all elements that are contained in
     * both given lists.
     *
     * @param &lt;E&gt; the element type
     * @param list1  the first list
     * @param list2  the second list
     * @return  the intersection of those two lists
     * @throws NullPointerException if either list is null
     */
    public static &lt;E&gt; List&lt;E&gt; intersection(final List&lt;? extends E&gt; list1, final List&lt;? extends E&gt; list2) {
	final List&lt;E&gt; result = new ArrayList&lt;&gt;();

	List&lt;? extends E&gt; smaller = list1;
	List&lt;? extends E&gt; larger = list2;
	if (list1.size() &gt; list2.size()) {
	    smaller = list2;
	    larger = list1;
	}

	final HashSet&lt;E&gt; hashSet = new HashSet&lt;&gt;(smaller);

	for (final E e : larger) {
	    if (hashSet.contains(e)) {
		result.add(e);
		hashSet.remove(e);
	    }
	}
	return result;
    }

    /**
     * Subtracts all elements in the second list from the first list,
     * placing the results in a new list.
     * &lt;p&gt;
     * This differs from {@link List#removeAll(Collection)} in that
     * cardinality is respected; if &lt;Code&gt;list1&lt;/Code&gt; contains two
     * occurrences of &lt;Code&gt;null&lt;/Code&gt; and &lt;Code&gt;list2&lt;/Code&gt; only
     * contains one occurrence, then the returned list will still contain
     * one occurrence.
     *
     * @param &lt;E&gt; the element type
     * @param list1  the list to subtract from
     * @param list2  the list to subtract
     * @return a new list containing the results
     * @throws NullPointerException if either list is null
     */
    public static &lt;E&gt; List&lt;E&gt; subtract(final List&lt;E&gt; list1, final List&lt;? extends E&gt; list2) {
	final ArrayList&lt;E&gt; result = new ArrayList&lt;&gt;();
	final HashBag&lt;E&gt; bag = new HashBag&lt;&gt;(list2);
	for (final E e : list1) {
	    if (!bag.remove(e, 1)) {
		result.add(e);
	    }
	}
	return result;
    }

}

