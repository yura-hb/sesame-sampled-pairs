import java.util.NoSuchElementException;

class CircularFifoQueue&lt;E&gt; extends AbstractCollection&lt;E&gt; implements Queue&lt;E&gt;, BoundedCollection&lt;E&gt;, Serializable {
    /**
     * Returns an iterator over this queue's elements.
     *
     * @return an iterator over this queue's elements
     */
    @Override
    public Iterator&lt;E&gt; iterator() {
	return new Iterator&lt;E&gt;() {

	    private int index = start;
	    private int lastReturnedIndex = -1;
	    private boolean isFirst = full;

	    @Override
	    public boolean hasNext() {
		return isFirst || index != end;
	    }

	    @Override
	    public E next() {
		if (!hasNext()) {
		    throw new NoSuchElementException();
		}
		isFirst = false;
		lastReturnedIndex = index;
		index = increment(index);
		return elements[lastReturnedIndex];
	    }

	    @Override
	    public void remove() {
		if (lastReturnedIndex == -1) {
		    throw new IllegalStateException();
		}

		// First element can be removed quickly
		if (lastReturnedIndex == start) {
		    CircularFifoQueue.this.remove();
		    lastReturnedIndex = -1;
		    return;
		}

		int pos = lastReturnedIndex + 1;
		if (start &lt; lastReturnedIndex && pos &lt; end) {
		    // shift in one part
		    System.arraycopy(elements, pos, elements, lastReturnedIndex, end - pos);
		} else {
		    // Other elements require us to shift the subsequent elements
		    while (pos != end) {
			if (pos &gt;= maxElements) {
			    elements[pos - 1] = elements[0];
			    pos = 0;
			} else {
			    elements[decrement(pos)] = elements[pos];
			    pos = increment(pos);
			}
		    }
		}

		lastReturnedIndex = -1;
		end = decrement(end);
		elements[end] = null;
		full = false;
		index = decrement(index);
	    }

	};
    }

    /** Array index of first (oldest) queue element. */
    private transient int start = 0;
    /** Flag to indicate if the queue is currently full. */
    private transient boolean full = false;
    /**
     * Index mod maxElements of the array position following the last queue
     * element.  Queue elements start at elements[start] and "wrap around"
     * elements[maxElements-1], ending at elements[decrement(end)].
     * For example, elements = {c,a,b}, start=1, end=1 corresponds to
     * the queue [a,b,c].
     */
    private transient int end = 0;
    /** Underlying storage array. */
    private transient E[] elements;
    /** Capacity of the queue. */
    private final int maxElements;

    /**
     * Increments the internal index.
     *
     * @param index  the index to increment
     * @return the updated index
     */
    private int increment(int index) {
	index++;
	if (index &gt;= maxElements) {
	    index = 0;
	}
	return index;
    }

    @Override
    public E remove() {
	if (isEmpty()) {
	    throw new NoSuchElementException("queue is empty");
	}

	final E element = elements[start];
	if (null != element) {
	    elements[start++] = null;

	    if (start &gt;= maxElements) {
		start = 0;
	    }
	    full = false;
	}
	return element;
    }

    /**
     * Decrements the internal index.
     *
     * @param index  the index to decrement
     * @return the updated index
     */
    private int decrement(int index) {
	index--;
	if (index &lt; 0) {
	    index = maxElements - 1;
	}
	return index;
    }

    /**
     * Returns true if this queue is empty; false otherwise.
     *
     * @return true if this queue is empty
     */
    @Override
    public boolean isEmpty() {
	return size() == 0;
    }

    /**
     * Returns the number of elements stored in the queue.
     *
     * @return this queue's size
     */
    @Override
    public int size() {
	int size = 0;

	if (end &lt; start) {
	    size = maxElements - start + end;
	} else if (end == start) {
	    size = full ? maxElements : 0;
	} else {
	    size = end - start;
	}

	return size;
    }

}

