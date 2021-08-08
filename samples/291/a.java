abstract class AbstractLinkedMap&lt;K, V&gt; extends AbstractHashedMap&lt;K, V&gt; implements OrderedMap&lt;K, V&gt; {
    /**
     * Gets the key at the specified index.
     *
     * @param index  the index to retrieve
     * @return the key at the specified index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    protected LinkEntry&lt;K, V&gt; getEntry(final int index) {
	if (index &lt; 0) {
	    throw new IndexOutOfBoundsException("Index " + index + " is less than zero");
	}
	if (index &gt;= size) {
	    throw new IndexOutOfBoundsException("Index " + index + " is invalid for size " + size);
	}
	LinkEntry&lt;K, V&gt; entry;
	if (index &lt; size / 2) {
	    // Search forwards
	    entry = header.after;
	    for (int currentIndex = 0; currentIndex &lt; index; currentIndex++) {
		entry = entry.after;
	    }
	} else {
	    // Search backwards
	    entry = header;
	    for (int currentIndex = size; currentIndex &gt; index; currentIndex--) {
		entry = entry.before;
	    }
	}
	return entry;
    }

    /** Header in the linked list */
    transient LinkEntry&lt;K, V&gt; header;

}

