import java.lang.ref.ReferenceQueue;

class WeakHashMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements Map&lt;K, V&gt; {
    /**
     * Returns the entry associated with the specified key in this map.
     * Returns null if the map contains no mapping for this key.
     */
    Entry&lt;K, V&gt; getEntry(Object key) {
	Object k = maskNull(key);
	int h = hash(k);
	Entry&lt;K, V&gt;[] tab = getTable();
	int index = indexFor(h, tab.length);
	Entry&lt;K, V&gt; e = tab[index];
	while (e != null && !(e.hash == h && eq(k, e.get())))
	    e = e.next;
	return e;
    }

    /**
     * Value representing null keys inside tables.
     */
    private static final Object NULL_KEY = new Object();
    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    Entry&lt;K, V&gt;[] table;
    /**
     * Reference queue for cleared WeakEntries
     */
    private final ReferenceQueue&lt;Object&gt; queue = new ReferenceQueue&lt;&gt;();
    /**
     * The number of key-value mappings contained in this weak hash map.
     */
    private int size;

    /**
     * Use NULL_KEY for key if it is null.
     */
    private static Object maskNull(Object key) {
	return (key == null) ? NULL_KEY : key;
    }

    /**
     * Retrieve object hash code and applies a supplemental hash function to the
     * result hash, which defends against poor quality hash functions.  This is
     * critical because HashMap uses power-of-two length hash tables, that
     * otherwise encounter collisions for hashCodes that do not differ
     * in lower bits.
     */
    final int hash(Object k) {
	int h = k.hashCode();

	// This function ensures that hashCodes that differ only by
	// constant multiples at each bit position have a bounded
	// number of collisions (approximately 8 at default load factor).
	h ^= (h &gt;&gt;&gt; 20) ^ (h &gt;&gt;&gt; 12);
	return h ^ (h &gt;&gt;&gt; 7) ^ (h &gt;&gt;&gt; 4);
    }

    /**
     * Returns the table after first expunging stale entries.
     */
    private Entry&lt;K, V&gt;[] getTable() {
	expungeStaleEntries();
	return table;
    }

    /**
     * Returns index for hash code h.
     */
    private static int indexFor(int h, int length) {
	return h & (length - 1);
    }

    /**
     * Checks for equality of non-null reference x and possibly-null y.  By
     * default uses Object.equals.
     */
    private static boolean eq(Object x, Object y) {
	return x == y || x.equals(y);
    }

    /**
     * Expunges stale entries from the table.
     */
    private void expungeStaleEntries() {
	for (Object x; (x = queue.poll()) != null;) {
	    synchronized (queue) {
		@SuppressWarnings("unchecked")
		Entry&lt;K, V&gt; e = (Entry&lt;K, V&gt;) x;
		int i = indexFor(e.hash, table.length);

		Entry&lt;K, V&gt; prev = table[i];
		Entry&lt;K, V&gt; p = prev;
		while (p != null) {
		    Entry&lt;K, V&gt; next = p.next;
		    if (p == e) {
			if (prev == e)
			    table[i] = next;
			else
			    prev.next = next;
			// Must not null out e.next;
			// stale entries may be in use by a HashIterator
			e.value = null; // Help GC
			size--;
			break;
		    }
		    prev = p;
		    p = next;
		}
	    }
	}
    }

}

