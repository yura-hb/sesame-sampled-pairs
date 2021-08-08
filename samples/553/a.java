import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

class OpenIntToDoubleHashMap implements Serializable {
    class Iterator {
	/**
	 * Get the key of current entry.
	 * @return key of current entry
	 * @exception ConcurrentModificationException if the map is modified during iteration
	 * @exception NoSuchElementException if there is no element left in the map
	 */
	public int key() throws ConcurrentModificationException, NoSuchElementException {
	    if (referenceCount != count) {
		throw new ConcurrentModificationException();
	    }
	    if (current &lt; 0) {
		throw new NoSuchElementException();
	    }
	    return keys[current];
	}

	/** Reference modification count. */
	private final int referenceCount;
	/** Index of current element. */
	private int current;

    }

    /** Modifications count. */
    private transient int count;
    /** Keys table. */
    private int[] keys;

}

