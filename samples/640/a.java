class DefaultEquator&lt;T&gt; implements Equator&lt;T&gt;, Serializable {
    /**
     * {@inheritDoc} Delegates to {@link Object#equals(Object)}.
     */
    @Override
    public boolean equate(final T o1, final T o2) {
	return o1 == o2 || o1 != null && o1.equals(o2);
    }

}

