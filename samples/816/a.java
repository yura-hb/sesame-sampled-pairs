class ImmutableListMultimap&lt;K, V&gt; extends ImmutableMultimap&lt;K, V&gt; implements ListMultimap&lt;K, V&gt; {
    class Builder&lt;K, V&gt; extends Builder&lt;K, V&gt; {
	/** Returns a newly-created immutable list multimap. */
	@Override
	public ImmutableListMultimap&lt;K, V&gt; build() {
	    return (ImmutableListMultimap&lt;K, V&gt;) super.build();
	}

    }

}

