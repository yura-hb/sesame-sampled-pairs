class CollectionUtils {
    /**
     * Finds the first element in the given collection which matches the given predicate.
     * &lt;p&gt;
     * If the input collection or predicate is null, or no element of the collection
     * matches the predicate, null is returned.
     *
     * @param &lt;T&gt;  the type of object the {@link Iterable} contains
     * @param collection  the collection to search, may be null
     * @param predicate  the predicate to use, may be null
     * @return the first element of the collection which matches the predicate or null if none could be found
     * @deprecated since 4.1, use {@link IterableUtils#find(Iterable, Predicate)} instead
     */
    @Deprecated
    public static &lt;T&gt; T find(final Iterable&lt;T&gt; collection, final Predicate&lt;? super T&gt; predicate) {
	return predicate != null ? IterableUtils.find(collection, predicate) : null;
    }

}

