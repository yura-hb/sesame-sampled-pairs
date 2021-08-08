abstract class AbstractMultiSet&lt;E&gt; extends AbstractCollection&lt;E&gt; implements MultiSet&lt;E&gt; {
    /**
     * Returns an unmodifiable view of the entries of this multiset.
     *
     * @return the set of entries in this multiset
     */
    @Override
    public Set&lt;Entry&lt;E&gt;&gt; entrySet() {
	if (entrySet == null) {
	    entrySet = createEntrySet();
	}
	return entrySet;
    }

    /** View of the entries */
    private transient Set&lt;Entry&lt;E&gt;&gt; entrySet;

    /**
     * Create a new view for the set of entries in this multiset.
     *
     * @return a view of the set of entries
     */
    protected Set&lt;Entry&lt;E&gt;&gt; createEntrySet() {
	return new EntrySet&lt;&gt;(this);
    }

    class EntrySet&lt;E&gt; extends AbstractSet&lt;Entry&lt;E&gt;&gt; {
	/** View of the entries */
	private transient Set&lt;Entry&lt;E&gt;&gt; entrySet;

	/**
	 * Constructs a new view of the MultiSet.
	 *
	 * @param parent  the parent MultiSet
	 */
	protected EntrySet(final AbstractMultiSet&lt;E&gt; parent) {
	    this.parent = parent;
	}

    }

}

