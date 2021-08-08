import org.apache.commons.collections4.iterators.EmptyIterator;

class AbstractHashedMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements IterableMap&lt;K, V&gt; {
    /**
     * Creates a key set iterator.
     * Subclasses can override this to return iterators with different properties.
     *
     * @return the keySet iterator
     */
    protected Iterator&lt;K&gt; createKeySetIterator() {
	if (size() == 0) {
	    return EmptyIterator.&lt;K&gt;emptyIterator();
	}
	return new KeySetIterator&lt;&gt;(this);
    }

    /** The size of the map */
    transient int size;
    /** Map entries */
    transient HashEntry&lt;K, V&gt;[] data;
    /** Modification count for iterators */
    transient int modCount;

    /**
     * Gets the size of the map.
     *
     * @return the size
     */
    @Override
    public int size() {
	return size;
    }

    class KeySetIterator&lt;K&gt; extends HashIterator&lt;K, Object&gt; implements Iterator&lt;K&gt; {
	/** The size of the map */
	transient int size;
	/** Map entries */
	transient HashEntry&lt;K, V&gt;[] data;
	/** Modification count for iterators */
	transient int modCount;

	@SuppressWarnings("unchecked")
	protected KeySetIterator(final AbstractHashedMap&lt;K, ?&gt; parent) {
	    super((AbstractHashedMap&lt;K, Object&gt;) parent);
	}

    }

    abstract class HashIterator&lt;K, V&gt; {
	/** The size of the map */
	transient int size;
	/** Map entries */
	transient HashEntry&lt;K, V&gt;[] data;
	/** Modification count for iterators */
	transient int modCount;

	protected HashIterator(final AbstractHashedMap&lt;K, V&gt; parent) {
	    super();
	    this.parent = parent;
	    final HashEntry&lt;K, V&gt;[] data = parent.data;
	    int i = data.length;
	    HashEntry&lt;K, V&gt; next = null;
	    while (i &gt; 0 && next == null) {
		next = data[--i];
	    }
	    this.next = next;
	    this.hashIndex = i;
	    this.expectedModCount = parent.modCount;
	}

    }

}

