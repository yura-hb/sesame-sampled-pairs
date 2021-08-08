import java.lang.ref.ReferenceQueue;

class LocalCache&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt; {
    class Segment&lt;K, V&gt; extends ReentrantLock {
	/** Clears all entries from the key and value reference queues. */
	void clearReferenceQueues() {
	    if (map.usesKeyReferences()) {
		clearKeyReferenceQueue();
	    }
	    if (map.usesValueReferences()) {
		clearValueReferenceQueue();
	    }
	}

	@Weak
	final LocalCache&lt;K, V&gt; map;
	/**
	 * The key reference queue contains entries whose keys have been garbage collected, and which
	 * need to be cleaned up internally.
	 */
	final @Nullable ReferenceQueue&lt;K&gt; keyReferenceQueue;
	/**
	 * The value reference queue contains value references whose values have been garbage collected,
	 * and which need to be cleaned up internally.
	 */
	final @Nullable ReferenceQueue&lt;V&gt; valueReferenceQueue;

	void clearKeyReferenceQueue() {
	    while (keyReferenceQueue.poll() != null) {
	    }
	}

	void clearValueReferenceQueue() {
	    while (valueReferenceQueue.poll() != null) {
	    }
	}

    }

    /** Strategy for referencing keys. */
    final Strength keyStrength;
    /** Strategy for referencing values. */
    final Strength valueStrength;

    boolean usesKeyReferences() {
	return keyStrength != Strength.STRONG;
    }

    boolean usesValueReferences() {
	return valueStrength != Strength.STRONG;
    }

}

