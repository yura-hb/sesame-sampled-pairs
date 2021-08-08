import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;

class WeakHashMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements Map&lt;K, V&gt; {
    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for any
     * of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map.
     * @throws  NullPointerException if the specified map is null.
     */
    public void putAll(Map&lt;? extends K, ? extends V&gt; m) {
	int numKeysToBeAdded = m.size();
	if (numKeysToBeAdded == 0)
	    return;

	/*
	 * Expand the map if the map if the number of mappings to be added
	 * is greater than or equal to threshold.  This is conservative; the
	 * obvious condition is (m.size() + size) &gt;= threshold, but this
	 * condition could result in a map with twice the appropriate capacity,
	 * if the keys to be added overlap with the keys already in this map.
	 * By using the conservative calculation, we subject ourself
	 * to at most one extra resize.
	 */
	if (numKeysToBeAdded &gt; threshold) {
	    int targetCapacity = (int) (numKeysToBeAdded / loadFactor + 1);
	    if (targetCapacity &gt; MAXIMUM_CAPACITY)
		targetCapacity = MAXIMUM_CAPACITY;
	    int newCapacity = table.length;
	    while (newCapacity &lt; targetCapacity)
		newCapacity &lt;&lt;= 1;
	    if (newCapacity &gt; table.length)
		resize(newCapacity);
	}

	for (Map.Entry&lt;? extends K, ? extends V&gt; e : m.entrySet())
	    put(e.getKey(), e.getValue());
    }

    /**
     * The next size value at which to resize (capacity * load factor).
     */
    private int threshold;
    /**
     * The load factor for the hash table.
     */
    private final float loadFactor;
    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two &lt;= 1&lt;&lt;30.
     */
    private static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    Entry&lt;K, V&gt;[] table;
    /**
     * The number of key-value mappings contained in this weak hash map.
     */
    private int size;
    /**
     * The number of times this WeakHashMap has been structurally modified.
     * Structural modifications are those that change the number of
     * mappings in the map or otherwise modify its internal structure
     * (e.g., rehash).  This field is used to make iterators on
     * Collection-views of the map fail-fast.
     *
     * @see ConcurrentModificationException
     */
    int modCount;
    /**
     * Reference queue for cleared WeakEntries
     */
    private final ReferenceQueue&lt;Object&gt; queue = new ReferenceQueue&lt;&gt;();
    /**
     * Value representing null keys inside tables.
     */
    private static final Object NULL_KEY = new Object();

    /**
     * Rehashes the contents of this map into a new array with a
     * larger capacity.  This method is called automatically when the
     * number of keys in this map reaches its threshold.
     *
     * If current capacity is MAXIMUM_CAPACITY, this method does not
     * resize the map, but sets threshold to Integer.MAX_VALUE.
     * This has the effect of preventing future calls.
     *
     * @param newCapacity the new capacity, MUST be a power of two;
     *        must be greater than current capacity unless current
     *        capacity is MAXIMUM_CAPACITY (in which case value
     *        is irrelevant).
     */
    void resize(int newCapacity) {
	Entry&lt;K, V&gt;[] oldTable = getTable();
	int oldCapacity = oldTable.length;
	if (oldCapacity == MAXIMUM_CAPACITY) {
	    threshold = Integer.MAX_VALUE;
	    return;
	}

	Entry&lt;K, V&gt;[] newTable = newTable(newCapacity);
	transfer(oldTable, newTable);
	table = newTable;

	/*
	 * If ignoring null elements and processing ref queue caused massive
	 * shrinkage, then restore old table.  This should be rare, but avoids
	 * unbounded expansion of garbage-filled tables.
	 */
	if (size &gt;= threshold / 2) {
	    threshold = (int) (newCapacity * loadFactor);
	} else {
	    expungeStaleEntries();
	    transfer(newTable, oldTable);
	    table = oldTable;
	}
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with {@code key}.)
     */
    public V put(K key, V value) {
	Object k = maskNull(key);
	int h = hash(k);
	Entry&lt;K, V&gt;[] tab = getTable();
	int i = indexFor(h, tab.length);

	for (Entry&lt;K, V&gt; e = tab[i]; e != null; e = e.next) {
	    if (h == e.hash && eq(k, e.get())) {
		V oldValue = e.value;
		if (value != oldValue)
		    e.value = value;
		return oldValue;
	    }
	}

	modCount++;
	Entry&lt;K, V&gt; e = tab[i];
	tab[i] = new Entry&lt;&gt;(k, value, queue, h, e);
	if (++size &gt;= threshold)
	    resize(tab.length * 2);
	return null;
    }

    /**
     * Returns the table after first expunging stale entries.
     */
    private Entry&lt;K, V&gt;[] getTable() {
	expungeStaleEntries();
	return table;
    }

    @SuppressWarnings("unchecked")
    private Entry&lt;K, V&gt;[] newTable(int n) {
	return (Entry&lt;K, V&gt;[]) new Entry&lt;?, ?&gt;[n];
    }

    /** Transfers all entries from src to dest tables */
    private void transfer(Entry&lt;K, V&gt;[] src, Entry&lt;K, V&gt;[] dest) {
	for (int j = 0; j &lt; src.length; ++j) {
	    Entry&lt;K, V&gt; e = src[j];
	    src[j] = null;
	    while (e != null) {
		Entry&lt;K, V&gt; next = e.next;
		Object key = e.get();
		if (key == null) {
		    e.next = null; // Help GC
		    e.value = null; //  "   "
		    size--;
		} else {
		    int i = indexFor(e.hash, dest.length);
		    e.next = dest[i];
		    dest[i] = e;
		}
		e = next;
	    }
	}
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

    class Entry&lt;K, V&gt; extends WeakReference&lt;Object&gt; implements Entry&lt;K, V&gt; {
	/**
	* The next size value at which to resize (capacity * load factor).
	*/
	private int threshold;
	/**
	* The load factor for the hash table.
	*/
	private final float loadFactor;
	/**
	* The maximum capacity, used if a higher value is implicitly specified
	* by either of the constructors with arguments.
	* MUST be a power of two &lt;= 1&lt;&lt;30.
	*/
	private static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
	/**
	* The table, resized as necessary. Length MUST Always be a power of two.
	*/
	Entry&lt;K, V&gt;[] table;
	/**
	* The number of key-value mappings contained in this weak hash map.
	*/
	private int size;
	/**
	* The number of times this WeakHashMap has been structurally modified.
	* Structural modifications are those that change the number of
	* mappings in the map or otherwise modify its internal structure
	* (e.g., rehash).  This field is used to make iterators on
	* Collection-views of the map fail-fast.
	*
	* @see ConcurrentModificationException
	*/
	int modCount;
	/**
	* Reference queue for cleared WeakEntries
	*/
	private final ReferenceQueue&lt;Object&gt; queue = new ReferenceQueue&lt;&gt;();
	/**
	* Value representing null keys inside tables.
	*/
	private static final Object NULL_KEY = new Object();

	/**
	 * Creates new entry.
	 */
	Entry(Object key, V value, ReferenceQueue&lt;Object&gt; queue, int hash, Entry&lt;K, V&gt; next) {
	    super(key, queue);
	    this.value = value;
	    this.hash = hash;
	    this.next = next;
	}

    }

}

