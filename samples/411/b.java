class ImmutableSetMultimap&lt;K, V&gt; extends ImmutableMultimap&lt;K, V&gt; implements SetMultimap&lt;K, V&gt; {
    /** Returns the empty multimap. */
    // Casting is safe because the multimap will never hold any elements.
    @SuppressWarnings("unchecked")
    public static &lt;K, V&gt; ImmutableSetMultimap&lt;K, V&gt; of() {
	return (ImmutableSetMultimap&lt;K, V&gt;) EmptyImmutableSetMultimap.INSTANCE;
    }

}

