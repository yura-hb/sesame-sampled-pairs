abstract class ImmutableMultimap&lt;K, V&gt; extends BaseImmutableMultimap&lt;K, V&gt; implements Serializable {
    /** Returns an immutable collection of all key-value pairs in the multimap. */
    @Override
    public ImmutableCollection&lt;Entry&lt;K, V&gt;&gt; entries() {
	return (ImmutableCollection&lt;Entry&lt;K, V&gt;&gt;) super.entries();
    }

}

