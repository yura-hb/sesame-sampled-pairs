import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

class OpenIntToDoubleHashMap implements Serializable {
    class Iterator {
	/**
	 * Advance iterator one step further.
	 * @exception ConcurrentModificationException if the map is modified during iteration
	 * @exception NoSuchElementException if there is no element left in the map
	 */
	public void advance() throws ConcurrentModificationException, NoSuchElementException {

	    if (referenceCount != count) {
		throw new ConcurrentModificationException();
	    }

	    // advance on step
	    current = next;

	    // prepare next step
	    try {
		while (states[++next] != FULL) { // NOPMD
		    // nothing to do
		}
	    } catch (ArrayIndexOutOfBoundsException e) {
		next = -2;
		if (current &lt; 0) {
		    throw new NoSuchElementException();
		}
	    }

	}

	/** Reference modification count. */
	private final int referenceCount;
	/** Index of current element. */
	private int current;
	/** Index of next element. */
	private int next;

    }

    /** Modifications count. */
    private transient int count;
    /** States table. */
    private byte[] states;
    /** Status indicator for full table entries. */
    protected static final byte FULL = 1;

}

