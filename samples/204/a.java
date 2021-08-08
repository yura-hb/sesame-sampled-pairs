import java.util.Collection;

class IterableUtils {
    /**
     * Returns a view of the given iterable which will cycle infinitely over
     * its elements.
     * &lt;p&gt;
     * The returned iterable's iterator supports {@code remove()} if
     * {@code iterable.iterator()} does. After {@code remove()} is called, subsequent
     * cycles omit the removed element, which is no longer in {@code iterable}. The
     * iterator's {@code hasNext()} method returns {@code true} until {@code iterable}
     * is empty.
     *
     * @param &lt;E&gt; the element type
     * @param iterable  the iterable to loop, may not be null
     * @return a view of the iterable, providing an infinite loop over its elements
     * @throws NullPointerException if iterable is null
     */
    public static &lt;E&gt; Iterable&lt;E&gt; loopingIterable(final Iterable&lt;E&gt; iterable) {
	checkNotNull(iterable);
	return new FluentIterable&lt;E&gt;() {
	    @Override
	    public Iterator&lt;E&gt; iterator() {
		return new LazyIteratorChain&lt;E&gt;() {
		    @Override
		    protected Iterator&lt;? extends E&gt; nextIterator(final int count) {
			if (IterableUtils.isEmpty(iterable)) {
			    return null;
			}
			return iterable.iterator();
		    }
		};
	    }
	};
    }

    /**
     * Fail-fast check for null arguments.
     *
     * @param iterable  the iterable to check
     * @throws NullPointerException if iterable is null
     */
    static void checkNotNull(final Iterable&lt;?&gt; iterable) {
	if (iterable == null) {
	    throw new NullPointerException("Iterable must not be null.");
	}
    }

    /**
     * Answers true if the provided iterable is empty.
     * &lt;p&gt;
     * A &lt;code&gt;null&lt;/code&gt; iterable returns true.
     *
     * @param iterable  the {@link Iterable to use}, may be null
     * @return true if the iterable is null or empty, false otherwise
     */
    public static boolean isEmpty(final Iterable&lt;?&gt; iterable) {
	if (iterable instanceof Collection&lt;?&gt;) {
	    return ((Collection&lt;?&gt;) iterable).isEmpty();
	}
	return IteratorUtils.isEmpty(emptyIteratorIfNull(iterable));
    }

    /**
     * Returns an empty iterator if the argument is &lt;code&gt;null&lt;/code&gt;,
     * or {@code iterable.iterator()} otherwise.
     *
     * @param &lt;E&gt; the element type
     * @param iterable  the iterable, possibly &lt;code&gt;null&lt;/code&gt;
     * @return an empty iterator if the argument is &lt;code&gt;null&lt;/code&gt;
     */
    private static &lt;E&gt; Iterator&lt;E&gt; emptyIteratorIfNull(final Iterable&lt;E&gt; iterable) {
	return iterable != null ? iterable.iterator() : IteratorUtils.&lt;E&gt;emptyIterator();
    }

}

