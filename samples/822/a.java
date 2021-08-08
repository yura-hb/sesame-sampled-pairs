import org.apache.commons.math4.exception.NullArgumentException;

class IntegerSequence {
    class Incrementor implements Iterator&lt;Integer&gt; {
	/**
	 * Creates a new instance with a given initial value.
	 * The counter is reset to the initial value.
	 *
	 * @param start Initial value of the counter.
	 * @return a new instance.
	 */
	public Incrementor withStart(int start) {
	    return new Incrementor(start, this.maximalCount, this.increment, this.maxCountCallback);
	}

	/** Upper limit for the counter. */
	private final int maximalCount;
	/** Increment. */
	private final int increment;
	/** Function called at counter exhaustion. */
	private final MaxCountExceededCallback maxCountCallback;
	/** Initial value the counter. */
	private final int init;
	/** Current count. */
	private int count = 0;

	/**
	 * Creates an incrementor.
	 * The counter will be exhausted either when {@code max} is reached
	 * or when {@code nTimes} increments have been performed.
	 *
	 * @param start Initial value.
	 * @param max Maximal count.
	 * @param step Increment.
	 * @param cb Function to be called when the maximal count has been reached.
	 * @throws NullArgumentException if {@code cb} is {@code null}.
	 */
	private Incrementor(int start, int max, int step, MaxCountExceededCallback cb) throws NullArgumentException {
	    if (cb == null) {
		throw new NullArgumentException();
	    }
	    this.init = start;
	    this.maximalCount = max;
	    this.increment = step;
	    this.maxCountCallback = cb;
	    this.count = start;
	}

    }

}

