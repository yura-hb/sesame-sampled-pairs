import java.util.concurrent.locks.ReentrantLock;

class ConcurrentHashMapV7&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    /**
     * {@inheritDoc}
     *
     * @return the previous value associated with the specified key,
     *         or &lt;tt&gt;null&lt;/tt&gt; if there was no mapping for the key
     * @throws NullPointerException if the specified key or value is null
     */
    @Override
    public V replace(K key, V value) {
	int hash = hash(key);
	if (value == null) {
	    throw new NullPointerException();
	}
	Segment&lt;K, V&gt; s = segmentForHash(hash);
	return s == null ? null : s.replace(key, hash, value);
    }

    /**
     * A randomizing value associated with this instance that is applied to
     * hash code of keys to make hash collisions harder to find.
     */
    private transient final int hashSeed = randomHashSeed(this);
    /**
     * Shift value for indexing within segments.
     */
    final int segmentShift;
    /**
     * Mask value for indexing into segments. The upper bits of a
     * key's hash code are used to choose the segment.
     */
    final int segmentMask;
    private static final int SSHIFT;
    private static final long SBASE;
    private static final sun.misc.Unsafe UNSAFE;
    /**
     * The segments, each of which is a specialized hash table.
     */
    final Segment&lt;K, V&gt;[] segments;
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
     * Get the segment for the given hash
     */
    @SuppressWarnings("unchecked")
    private Segment&lt;K, V&gt; segmentForHash(int h) {
	long u = (((h &gt;&gt;&gt; segmentShift) & segmentMask) &lt;&lt; SSHIFT) + SBASE;
	return (Segment&lt;K, V&gt;) UNSAFE.getObjectVolatile(segments, u);
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
	* A randomizing value associated with this instance that is applied to
	* hash code of keys to make hash collisions harder to find.
	*/
	private transient final int hashSeed = randomHashSeed(this);
	/**
	* Shift value for indexing within segments.
	*/
	final int segmentShift;
	/**
	* Mask value for indexing into segments. The upper bits of a
	* key's hash code are used to choose the segment.
	*/
	final int segmentMask;
	private static final int SSHIFT;
	private static final long SBASE;
	private static final sun.misc.Unsafe UNSAFE;
	/**
	* The segments, each of which is a specialized hash table.
	*/
	final Segment&lt;K, V&gt;[] segments;
	private static final int TSHIFT;
	private static final long TBASE;

	final V replace(K key, int hash, V value) {
	    if (!tryLock()) {
		scanAndLock(key, hash);
	    }
	    V oldValue = null;
	    try {
		HashEntry&lt;K, V&gt; e;
		for (e = entryForHash(this, hash); e != null; e = e.next) {
		    K k;
		    if ((k = e.key) == key || (e.hash == hash && key.equals(k))) {
			oldValue = e.value;
			e.value = value;
			++modCount;
			break;
		    }
		}
	    } finally {
		unlock();
	    }
	    return oldValue;
	}

	/**
	 * Scans for a node containing the given key while trying to
	 * acquire lock for a remove or replace operation. Upon
	 * return, guarantees that lock is held.  Note that we must
	 * lock even if the key is not found, to ensure sequential
	 * consistency of updates.
	 */
	private void scanAndLock(Object key, int hash) {
	    // similar to but simpler than scanAndLockForPut
	    HashEntry&lt;K, V&gt; first = entryForHash(this, hash);
	    HashEntry&lt;K, V&gt; e = first;
	    int retries = -1;
	    while (!tryLock()) {
		HashEntry&lt;K, V&gt; f;
		if (retries &lt; 0) {
		    if (e == null || key.equals(e.key)) {
			retries = 0;
		    } else {
			e = e.next;
		    }
		} else if (++retries &gt; MAX_SCAN_RETRIES) {
		    lock();
		    break;
		} else if ((retries & 1) == 0 && (f = entryForHash(this, hash)) != first) {
		    e = first = f;
		    retries = -1;
		}
	    }
	}

    }

}

