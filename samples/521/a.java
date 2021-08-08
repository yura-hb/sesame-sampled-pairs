class LRUMap&lt;K, V&gt; extends AbstractLinkedMap&lt;K, V&gt; implements BoundedMap&lt;K, V&gt;, Serializable, Cloneable {
    /**
     * Moves an entry to the MRU position at the end of the list.
     * &lt;p&gt;
     * This implementation moves the updated entry to the end of the list.
     *
     * @param entry  the entry to update
     */
    protected void moveToMRU(final LinkEntry&lt;K, V&gt; entry) {
	if (entry.after != header) {
	    modCount++;
	    // remove
	    if (entry.before == null) {
		throw new IllegalStateException("Entry.before is null."
			+ " Please check that your keys are immutable, and that you have used synchronization properly."
			+ " If so, then please report this to dev@commons.apache.org as a bug.");
	    }
	    entry.before.after = entry.after;
	    entry.after.before = entry.before;
	    // add first
	    entry.after = header;
	    entry.before = header.before;
	    header.before.after = entry;
	    header.before = entry;
	} else if (entry == header) {
	    throw new IllegalStateException(
		    "Can't move header to MRU" + " (please report this to dev@commons.apache.org)");
	}
    }

}

