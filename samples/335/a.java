class IterableUtils {
    /**
     * Combines two iterables into a single iterable.
     * &lt;p&gt;
     * The returned iterable has an iterator that traverses the elements in {@code a},
     * followed by the elements in {@code b}. The source iterators are not polled until
     * necessary.
     * &lt;p&gt;
     * The returned iterable's iterator supports {@code remove()} when the corresponding
     * input iterator supports it.
     *
     * @param &lt;E&gt; the element type
     * @param a  the first iterable, may not be null
     * @param b  the second iterable, may not be null
     * @return a new iterable, combining the provided iterables
     * @throws NullPointerException if either a or b is null
     */
    @SuppressWarnings("unchecked")
    public static &lt;E&gt; Iterable&lt;E&gt; chainedIterable(final Iterable&lt;? extends E&gt; a, final Iterable&lt;? extends E&gt; b) {
	return chainedIterable(new Iterable[] { a, b });
    }

    /**
     * Combines the provided iterables into a single iterable.
     * &lt;p&gt;
     * The returned iterable has an iterator that traverses the elements in the order
     * of the arguments, i.e. iterables[0], iterables[1], .... The source iterators
     * are not polled until necessary.
     * &lt;p&gt;
     * The returned iterable's iterator supports {@code remove()} when the corresponding
     * input iterator supports it.
     *
     * @param &lt;E&gt; the element type
     * @param iterables  the iterables to combine, may not be null
     * @return a new iterable, combining the provided iterables
     * @throws NullPointerException if either of the provided iterables is null
     */
    public static &lt;E&gt; Iterable&lt;E&gt; chainedIterable(final Iterable&lt;? extends E&gt;... iterables) {
	checkNotNull(iterables);
	return new FluentIterable&lt;E&gt;() {
	    @Override
	    public Iterator&lt;E&gt; iterator() {
		return new LazyIteratorChain&lt;E&gt;() {
		    @Override
		    protected Iterator&lt;? extends E&gt; nextIterator(final int count) {
			if (count &gt; iterables.length) {
			    return null;
			}
			return iterables[count - 1].iterator();
		    }
		};
	    }
	};
    }

    /**
     * Fail-fast check for null arguments.
     *
     * @param iterables  the iterables to check
     * @throws NullPointerException if the argument or any of its contents is null
     */
    static void checkNotNull(final Iterable&lt;?&gt;... iterables) {
	if (iterables == null) {
	    throw new NullPointerException("Iterables must not be null.");
	}
	for (final Iterable&lt;?&gt; iterable : iterables) {
	    checkNotNull(iterable);
	}
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

}

