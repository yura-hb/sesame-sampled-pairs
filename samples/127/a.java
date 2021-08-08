import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

class OpenIntToFieldHashMap&lt;T&gt; implements Serializable {
    class Iterator {
	/**
	 * Get the value of current entry.
	 * @return value of current entry
	 * @exception ConcurrentModificationException if the map is modified during iteration
	 * @exception NoSuchElementException if there is no element left in the map
	 */
	public T value() throws ConcurrentModificationException, NoSuchElementException {
	    if (referenceCount != count) {
		throw new ConcurrentModificationException();
	    }
	    if (current &lt; 0) {
		throw new NoSuchElementException();
	    }
	    return values[current];
	}

	/** Reference modification count. */
	private final int referenceCount;
	/** Index of current element. */
	private int current;

    }

    /** Modifications count. */
    private transient int count;
    /** Values table. */
    private T[] values;

}

