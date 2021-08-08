import java.util.Map;
import java.util.Set;

class ManyToMany&lt;T1, T2&gt; {
    /**
     * Remove the key and its associated key/value entries.
     * Calling removeKey(k) is equivalent to calling remove(k,v) 
     * for every v in getValues(k).
     * @return true if the key existed in the map prior to removal
     */
    public synchronized boolean removeKey(T1 key) {
	// Remove all back-references to key.
	Set&lt;T2&gt; values = _forward.get(key);
	if (null == values) {
	    // key does not exist in map.
	    assert checkIntegrity();
	    return false;
	}
	for (T2 value : values) {
	    Set&lt;T1&gt; keys = _reverse.get(value);
	    if (null != keys) {
		keys.remove(key);
		if (keys.isEmpty()) {
		    _reverse.remove(value);
		}
	    }
	}
	// Now remove the forward references from key.
	_forward.remove(key);
	_dirty = true;
	assert checkIntegrity();
	return true;
    }

    private final Map&lt;T1, Set&lt;T2&gt;&gt; _forward = new HashMap&lt;&gt;();
    private final Map&lt;T2, Set&lt;T1&gt;&gt; _reverse = new HashMap&lt;&gt;();
    private boolean _dirty = false;

    /**
     * Check the integrity of the internal data structures.  This is intended to
     * be called within an assert, so that if asserts are disabled the integrity
     * checks will not cause a performance impact.
     * @return true if everything is okay.
     * @throws IllegalStateException if there is a problem.
     */
    private boolean checkIntegrity() {
	// For every T1-&gt;T2 mapping in the forward map, there should be a corresponding
	// T2-&gt;T1 mapping in the reverse map.
	for (Map.Entry&lt;T1, Set&lt;T2&gt;&gt; entry : _forward.entrySet()) {
	    Set&lt;T2&gt; values = entry.getValue();
	    if (values.isEmpty()) {
		throw new IllegalStateException("Integrity compromised: forward map contains an empty set"); //$NON-NLS-1$
	    }
	    for (T2 value : values) {
		Set&lt;T1&gt; keys = _reverse.get(value);
		if (null == keys || !keys.contains(entry.getKey())) {
		    throw new IllegalStateException(
			    "Integrity compromised: forward map contains an entry missing from reverse map: " + value); //$NON-NLS-1$
		}
	    }
	}
	// And likewise in the other direction.
	for (Map.Entry&lt;T2, Set&lt;T1&gt;&gt; entry : _reverse.entrySet()) {
	    Set&lt;T1&gt; keys = entry.getValue();
	    if (keys.isEmpty()) {
		throw new IllegalStateException("Integrity compromised: reverse map contains an empty set"); //$NON-NLS-1$
	    }
	    for (T1 key : keys) {
		Set&lt;T2&gt; values = _forward.get(key);
		if (null == values || !values.contains(entry.getKey())) {
		    throw new IllegalStateException(
			    "Integrity compromised: reverse map contains an entry missing from forward map: " + key); //$NON-NLS-1$
		}
	    }
	}
	return true;
    }

}

