import java.util.concurrent.locks.ReentrantLock;

class ConcurrentHashMapV7&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    /**
     * Maps the specified key to the specified value in this table.
     * Neither the key nor the value can be null.
     *
     * &lt;p&gt; The value can be retrieved by calling the &lt;tt&gt;get&lt;/tt&gt; method
     * with a key that is equal to the original key.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with &lt;tt&gt;key&lt;/tt&gt;, or
     *         &lt;tt&gt;null&lt;/tt&gt; if there was no mapping for &lt;tt&gt;key&lt;/tt&gt;
     * @throws NullPointerException if the specified key or value is null
     */
    @Override
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
	Segment&lt;K, V&gt; s;
	if (value == null) {
	    throw new NullPointerException();
	}
	int hash = hash(key);
	int j = (hash &gt;&gt;&gt; segmentShift) & segmentMask;
	if ((s = (Segment&lt;K, V&gt;) UNSAFE.getObject // nonvolatile; recheck
	(segments, (j &lt;&lt; SSHIFT) + SBASE)) == null) {
	    s = ensureSegment(j);
	}
	return s.put(key, hash, value, false);
    }

    /**
     * Shift value for indexing within segments.
     */
    final int segmentShift;
    /**
     * Mask value for indexing into segments. The upper bits of a
     * key's hash code are used to choose the segment.
     */
    final int segmentMask;
    private static final sun.misc.Unsafe UNSAFE;
    /**
     * The segments, each of which is a specialized hash table.
     */
    final Segment&lt;K, V&gt;[] segments;
    private static final int SSHIFT;
    private static final long SBASE;
    /**
     * A randomizing value associated with this instance that is applied to
     * hash code of keys to make hash collisions harder to find.
     */
    private transient final int hashSeed = randomHashSeed(this);
    /**
     * The maximum capacity, used if a higher value is implicitly
     * specified by either of the constructors with arguments.  MUST
     * be a power of two &lt;= 1&lt;&lt;30 to ensure that entries are indexable
     * using ints.
     */
    static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
    private static final int TSHIFT;
    private static final long TBASE;

    /**
     * Applies a supplemental hash function to a given hashCode, which
     * defends against poor quality hash functions.  This is critical
     * because ConcurrentHashMap uses power-of-two length hash tables,
     * that otherwise encounter collisions for hashCodes that do not
     * differ in lower or upper bits.
     */
    private int hash(Object k) {
	int h = hashSeed;

	//        if ((0 != h) && (k instanceof String)) {
	//            return sun.misc.Hashing.stringHash32((String) k);
	//        }

	h ^= k.hashCode();

	// Spread bits to regularize both segment and index locations,
	// using variant of single-word Wang/Jenkins hash.
	h += (h &lt;&lt; 15) ^ 0xffffcd7d;
	h ^= (h &gt;&gt;&gt; 10);
	h += (h &lt;&lt; 3);
	h ^= (h &gt;&gt;&gt; 6);
	h += (h &lt;&lt; 2) + (h &lt;&lt; 14);
	return h ^ (h &gt;&gt;&gt; 16);
    }

    /**
     * Returns the segment for the given index, creating it and
     * recording in segment table (via CAS) if not already present.
     *
     * @param k the index
     * @return the segment
     */
    @SuppressWarnings("unchecked")
    private Segment&lt;K, V&gt; ensureSegment(int k) {
	final Segment&lt;K, V&gt;[] ss = this.segments;
	long u = (k &lt;&lt; SSHIFT) + SBASE; // raw offset
	Segment&lt;K, V&gt; seg;
	if ((seg = (Segment&lt;K, V&gt;) UNSAFE.getObjectVolatile(ss, u)) == null) {
	    Segment&lt;K, V&gt; proto = ss[0]; // use segment 0 as prototype
	    int cap = proto.table.length;
	    float lf = proto.loadFactor;
	    int threshold = (int) (cap * lf);
	    HashEntry&lt;K, V&gt;[] tab = new HashEntry[cap];
	    if ((seg = (Segment&lt;K, V&gt;) UNSAFE.getObjectVolatile(ss, u)) == null) { // recheck
		Segment&lt;K, V&gt; s = new Segment&lt;K, V&gt;(lf, threshold, tab);
		while ((seg = (Segment&lt;K, V&gt;) UNSAFE.getObjectVolatile(ss, u)) == null) {
		    if (UNSAFE.compareAndSwapObject(ss, u, null, seg = s)) {
			break;
		    }
		}
	    }
	}
	return seg;
    }

    /**
     * Gets the ith element of given table (if nonnull) with volatile
     * read semantics. Note: This is manually integrated into a few
     * performance-sensitive methods to reduce call overhead.
     */
    @SuppressWarnings("unchecked")
    static final &lt;K, V&gt; HashEntry&lt;K, V&gt; entryAt(HashEntry&lt;K, V&gt;[] tab, int i) {
	return (tab == null) ? null : (HashEntry&lt;K, V&gt;) UNSAFE.getObjectVolatile(tab, ((long) i &lt;&lt; TSHIFT) + TBASE);
    }

    /**
     * Sets the ith element of given table, with volatile write
     * semantics. (See above about use of putOrderedObject.)
     */
    static final &lt;K, V&gt; void setEntryAt(HashEntry&lt;K, V&gt;[] tab, int i, HashEntry&lt;K, V&gt; e) {
	UNSAFE.putOrderedObject(tab, ((long) i &lt;&lt; TSHIFT) + TBASE, e);
    }

    /**
     * Gets the table entry for the given segment and hash
     */
    @SuppressWarnings("unchecked")
    static final &lt;K, V&gt; HashEntry&lt;K, V&gt; entryForHash(Segment&lt;K, V&gt; seg, int h) {
	HashEntry&lt;K, V&gt;[] tab;
	return (seg == null || (tab = seg.table) == null) ? null
		: (HashEntry&lt;K, V&gt;) UNSAFE.getObjectVolatile(tab, ((long) (((tab.length - 1) & h)) &lt;&lt; TSHIFT) + TBASE);
    }

    class Segment&lt;K, V&gt; extends ReentrantLock implements Serializable {
	/**
	* Shift value for indexing within segments.
	*/
	final int segmentShift;
	/**
	* Mask value for indexing into segments. The upper bits of a
	* key's hash code are used to choose the segment.
	*/
	final int segmentMask;
	private static final sun.misc.Unsafe UNSAFE;
	/**
	* The segments, each of which is a specialized hash table.
	*/
	final Segment&lt;K, V&gt;[] segments;
	private static final int SSHIFT;
	private static final long SBASE;
	/**
	* A randomizing value associated with this instance that is applied to
	* hash code of keys to make hash collisions harder to find.
	*/
	private transient final int hashSeed = randomHashSeed(this);
	/**
	* The maximum capacity, used if a higher value is implicitly
	* specified by either of the constructors with arguments.  MUST
	* be a power of two &lt;= 1&lt;&lt;30 to ensure that entries are indexable
	* using ints.
	*/
	static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
	private static final int TSHIFT;
	private static final long TBASE;

	final V put(K key, int hash, V value, boolean onlyIfAbsent) {
	    HashEntry&lt;K, V&gt; node = tryLock() ? null : scanAndLockForPut(key, hash, value);
	    V oldValue;
	    try {
		HashEntry&lt;K, V&gt;[] tab = table;
		int index = (tab.length - 1) & hash;
		HashEntry&lt;K, V&gt; first = entryAt(tab, index);
		for (HashEntry&lt;K, V&gt; e = first;;) {
		    if (e != null) {
			K k;
			if ((k = e.key) == key || (e.hash == hash && key.equals(k))) {
			    oldValue = e.value;
			    if (!onlyIfAbsent) {
				e.value = value;
				++modCount;
			    }
			    break;
			}
			e = e.next;
		    } else {
			if (node != null) {
			    node.setNext(first);
			} else {
			    node = new HashEntry&lt;K, V&gt;(hash, key, value, first);
			}
			int c = count + 1;
			if (c &gt; threshold && tab.length &lt; MAXIMUM_CAPACITY) {
			    rehash(node);
			} else {
			    setEntryAt(tab, index, node);
			}
			++modCount;
			count = c;
			oldValue = null;
			break;
		    }
		}
	    } finally {
		unlock();
	    }
	    return oldValue;
	}

	Segment(float lf, int threshold, HashEntry&lt;K, V&gt;[] tab) {
	    this.loadFactor = lf;
	    this.threshold = threshold;
	    this.table = tab;
	}

	/**
	 * Scans for a node containing given key while trying to
	 * acquire lock, creating and returning one if not found. Upon
	 * return, guarantees that lock is held. UNlike in most
	 * methods, calls to method equals are not screened: Since
	 * traversal speed doesn't matter, we might as well help warm
	 * up the associated code and accesses as well.
	 *
	 * @return a new node if key not found, else null
	 */
	private HashEntry&lt;K, V&gt; scanAndLockForPut(K key, int hash, V value) {
	    HashEntry&lt;K, V&gt; first = entryForHash(this, hash);
	    HashEntry&lt;K, V&gt; e = first;
	    HashEntry&lt;K, V&gt; node = null;
	    int retries = -1; // negative while locating node
	    while (!tryLock()) {
		HashEntry&lt;K, V&gt; f; // to recheck first below
		if (retries &lt; 0) {
		    if (e == null) {
			if (node == null) {
			    node = new HashEntry&lt;K, V&gt;(hash, key, value, null);
			}
			retries = 0;
		    } else if (key.equals(e.key)) {
			retries = 0;
		    } else {
			e = e.next;
		    }
		} else if (++retries &gt; MAX_SCAN_RETRIES) {
		    lock();
		    break;
		} else if ((retries & 1) == 0 && (f = entryForHash(this, hash)) != first) {
		    e = first = f; // re-traverse if entry changed
		    retries = -1;
		}
	    }
	    return node;
	}

	/**
	 * Doubles size of table and repacks entries, also adding the
	 * given node to new table
	 */
	@SuppressWarnings("unchecked")
	private void rehash(HashEntry&lt;K, V&gt; node) {
	    /*
	     * Reclassify nodes in each list to new table.  Because we
	     * are using power-of-two expansion, the elements from
	     * each bin must either stay at same index, or move with a
	     * power of two offset. We eliminate unnecessary node
	     * creation by catching cases where old nodes can be
	     * reused because their next fields won't change.
	     * Statistically, at the default threshold, only about
	     * one-sixth of them need cloning when a table
	     * doubles. The nodes they replace will be garbage
	     * collectable as soon as they are no longer referenced by
	     * any reader thread that may be in the midst of
	     * concurrently traversing table. Entry accesses use plain
	     * array indexing because they are followed by volatile
	     * table write.
	     */
	    HashEntry&lt;K, V&gt;[] oldTable = table;
	    int oldCapacity = oldTable.length;
	    int newCapacity = oldCapacity &lt;&lt; 1;
	    threshold = (int) (newCapacity * loadFactor);
	    HashEntry&lt;K, V&gt;[] newTable = new HashEntry[newCapacity];
	    int sizeMask = newCapacity - 1;
	    for (int i = 0; i &lt; oldCapacity; i++) {
		HashEntry&lt;K, V&gt; e = oldTable[i];
		if (e != null) {
		    HashEntry&lt;K, V&gt; next = e.next;
		    int idx = e.hash & sizeMask;
		    if (next == null) {
			newTable[idx] = e;
		    } else { // Reuse consecutive sequence at same slot
			HashEntry&lt;K, V&gt; lastRun = e;
			int lastIdx = idx;
			for (HashEntry&lt;K, V&gt; last = next; last != null; last = last.next) {
			    int k = last.hash & sizeMask;
			    if (k != lastIdx) {
				lastIdx = k;
				lastRun = last;
			    }
			}
			newTable[lastIdx] = lastRun;
			// Clone remaining nodes
			for (HashEntry&lt;K, V&gt; p = e; p != lastRun; p = p.next) {
			    V v = p.value;
			    int h = p.hash;
			    int k = h & sizeMask;
			    HashEntry&lt;K, V&gt; n = newTable[k];
			    newTable[k] = new HashEntry&lt;K, V&gt;(h, p.key, v, n);
			}
		    }
		}
	    }
	    int nodeIndex = node.hash & sizeMask; // add the new node
	    node.setNext(newTable[nodeIndex]);
	    newTable[nodeIndex] = node;
	    table = newTable;
	}

    }

    class HashEntry&lt;K, V&gt; {
	/**
	* Shift value for indexing within segments.
	*/
	final int segmentShift;
	/**
	* Mask value for indexing into segments. The upper bits of a
	* key's hash code are used to choose the segment.
	*/
	final int segmentMask;
	private static final sun.misc.Unsafe UNSAFE;
	/**
	* The segments, each of which is a specialized hash table.
	*/
	final Segment&lt;K, V&gt;[] segments;
	private static final int SSHIFT;
	private static final long SBASE;
	/**
	* A randomizing value associated with this instance that is applied to
	* hash code of keys to make hash collisions harder to find.
	*/
	private transient final int hashSeed = randomHashSeed(this);
	/**
	* The maximum capacity, used if a higher value is implicitly
	* specified by either of the constructors with arguments.  MUST
	* be a power of two &lt;= 1&lt;&lt;30 to ensure that entries are indexable
	* using ints.
	*/
	static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
	private static final int TSHIFT;
	private static final long TBASE;

	/**
	 * Sets next field with volatile write semantics.  (See above
	 * about use of putOrderedObject.)
	 */
	final void setNext(HashEntry&lt;K, V&gt; n) {
	    UNSAFE.putOrderedObject(this, nextOffset, n);
	}

	HashEntry(int hash, K key, V value, HashEntry&lt;K, V&gt; next) {
	    this.hash = hash;
	    this.key = key;
	    this.value = value;
	    this.next = next;
	}

    }

}

