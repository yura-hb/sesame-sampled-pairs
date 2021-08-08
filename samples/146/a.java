import java.util.Iterator;
import java.util.Set;

abstract class AbstractMultiSet&lt;E&gt; extends AbstractCollection&lt;E&gt; implements MultiSet&lt;E&gt; {
    /**
     * Clears the multiset removing all elements from the entrySet.
     */
    @Override
    public void clear() {
	final Iterator&lt;Entry&lt;E&gt;&gt; it = entrySet().iterator();
	while (it.hasNext()) {
	    it.next();
	    it.remove();
	}
    }

    /** View of the entries */
    private transient Set&lt;Entry&lt;E&gt;&gt; entrySet;

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

