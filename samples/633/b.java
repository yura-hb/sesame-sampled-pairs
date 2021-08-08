class PredicatedSortedSet&lt;E&gt; extends PredicatedSet&lt;E&gt; implements SortedSet&lt;E&gt; {
    /**
     * Gets the sorted set being decorated.
     *
     * @return the decorated sorted set
     */
    @Override
    protected SortedSet&lt;E&gt; decorated() {
	return (SortedSet&lt;E&gt;) super.decorated();
    }

}

