import gnu.trove.impl.hash.TObjectHash;
import java.util.*;

class THashMap&lt;K, V&gt; extends TObjectHash&lt;K&gt; implements TMap&lt;K, V&gt;, Externalizable {
    /**
     * rehashes the map to the new capacity.
     *
     * @param newCapacity an &lt;code&gt;int&lt;/code&gt; value
     */
    @SuppressWarnings({ "unchecked" })
    protected void rehash(int newCapacity) {
	int oldCapacity = _set.length;
	int oldSize = size();
	Object oldKeys[] = _set;
	V oldVals[] = _values;

	_set = new Object[newCapacity];
	Arrays.fill(_set, FREE);
	_values = (V[]) new Object[newCapacity];

	// Process entries from the old array, skipping free and removed slots. Put the
	// values into the appropriate place in the new array.
	int count = 0;
	for (int i = oldCapacity; i-- &gt; 0;) {
	    Object o = oldKeys[i];

	    if (o == FREE || o == REMOVED)
		continue;

	    int index = insertKey((K) o);
	    if (index &lt; 0) {
		throwObjectContractViolation(_set[(-index - 1)], o, size(), oldSize, oldKeys);
	    }
	    _values[index] = oldVals[i];
	    //
	    count++;
	}

	// Last check: size before and after should be the same
	reportPotentialConcurrentMod(size(), oldSize);
    }

    /**
     * the values of the  map
     */
    protected transient V[] _values;

}

