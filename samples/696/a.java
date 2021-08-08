import com.google.common.base.Equivalence;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;

class MapMakerInternalMap&lt;K, V, E, S&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    abstract class Segment&lt;K, V, E, S&gt; extends ReentrantLock {
	/** Removes an entry whose value has been garbage collected. */
	@CanIgnoreReturnValue
	boolean reclaimValue(K key, int hash, WeakValueReference&lt;K, V, E&gt; valueReference) {
	    lock();
	    try {
		int newCount = this.count - 1;
		AtomicReferenceArray&lt;E&gt; table = this.table;
		int index = hash & (table.length() - 1);
		E first = table.get(index);

		for (E e = first; e != null; e = e.getNext()) {
		    K entryKey = e.getKey();
		    if (e.getHash() == hash && entryKey != null && map.keyEquivalence.equivalent(key, entryKey)) {
			WeakValueReference&lt;K, V, E&gt; v = ((WeakValueEntry&lt;K, V, E&gt;) e).getValueReference();
			if (v == valueReference) {
			    ++modCount;
			    E newFirst = removeFromChain(first, e);
			    newCount = this.count - 1;
			    table.set(index, newFirst);
			    this.count = newCount; // write-volatile
			    return true;
			}
			return false;
		    }
		}

		return false;
	    } finally {
		unlock();
	    }
	}

	/**
	 * The number of live elements in this segment's region. This does not include unset elements
	 * which are awaiting cleanup.
	 */
	volatile int count;
	/** The per-segment table. */
	@MonotonicNonNull
	volatile AtomicReferenceArray&lt;E&gt; table;
	@Weak
	final MapMakerInternalMap&lt;K, V, E, S&gt; map;
	/**
	 * Number of updates that alter the size of the table. This is used during bulk-read methods to
	 * make sure they see a consistent snapshot: If modCounts change during a traversal of segments
	 * computing size or checking containsValue, then we might have an inconsistent view of state so
	 * (usually) must retry.
	 */
	int modCount;

	/**
	 * Removes an entry from within a table. All entries following the removed node can stay, but
	 * all preceding ones need to be cloned.
	 *
	 * &lt;p&gt;This method does not decrement count for the removed entry, but does decrement count for
	 * all partially collected entries which are skipped. As such callers which are modifying count
	 * must re-read it after calling removeFromChain.
	 *
	 * @param first the first entry of the table
	 * @param entry the entry being removed from the table
	 * @return the new first entry for the table
	 */
	@GuardedBy("this")
	E removeFromChain(E first, E entry) {
	    int newCount = count;
	    E newFirst = entry.getNext();
	    for (E e = first; e != entry; e = e.getNext()) {
		E next = copyEntry(e, newFirst);
		if (next != null) {
		    newFirst = next;
		} else {
		    newCount--;
		}
	    }
	    this.count = newCount;
	    return newFirst;
	}

	/** Returns a copy of the given {@code entry}. */
	E copyEntry(E original, E newNext) {
	    return this.map.entryHelper.copy(self(), original, newNext);
	}

	/**
	 * Returns {@code this} up-casted to the specific {@link Segment} implementation type {@code S}.
	 *
	 * &lt;p&gt;This method exists so that the {@link Segment} code can be generic in terms of {@code S},
	 * the type of the concrete implementation.
	 */
	abstract S self();

    }

    /** Strategy for comparing keys. */
    final Equivalence&lt;Object&gt; keyEquivalence;
    /** Strategy for handling entries and segments in a type-safe and efficient manner. */
    final transient InternalEntryHelper&lt;K, V, E, S&gt; entryHelper;

    interface InternalEntry&lt;K, V, E&gt; {
	/** Strategy for comparing keys. */
	final Equivalence&lt;Object&gt; keyEquivalence;
	/** Strategy for handling entries and segments in a type-safe and efficient manner. */
	final transient InternalEntryHelper&lt;K, V, E, S&gt; entryHelper;

	/** Gets the next entry in the chain. */
	E getNext();

	/** Gets the key for this entry. */
	K getKey();

	/** Gets the entry's hash. */
	int getHash();

    }

    interface WeakValueEntry&lt;K, V, E&gt; {
	/** Strategy for comparing keys. */
	final Equivalence&lt;Object&gt; keyEquivalence;
	/** Strategy for handling entries and segments in a type-safe and efficient manner. */
	final transient InternalEntryHelper&lt;K, V, E, S&gt; entryHelper;

	/** Gets the weak value reference held by entry. */
	WeakValueReference&lt;K, V, E&gt; getValueReference();

    }

    interface InternalEntryHelper&lt;K, V, E, S&gt; {
	/** Strategy for comparing keys. */
	final Equivalence&lt;Object&gt; keyEquivalence;
	/** Strategy for handling entries and segments in a type-safe and efficient manner. */
	final transient InternalEntryHelper&lt;K, V, E, S&gt; entryHelper;

	/**
	* Returns a freshly created entry, typed at the {@code E} type, for the given {@code segment},
	* that is a copy of the given {@code entry}.
	*/
	E copy(S segment, E entry, @Nullable E newNext);

    }

}

