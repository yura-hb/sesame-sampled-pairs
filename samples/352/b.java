import java.util.Arrays;

class CopyOnWriteArrayList&lt;E&gt; implements List&lt;E&gt;, RandomAccess, Cloneable, Serializable {
    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void add(int index, E element) {
	synchronized (lock) {
	    Object[] es = getArray();
	    int len = es.length;
	    if (index &gt; len || index &lt; 0)
		throw new IndexOutOfBoundsException(outOfBounds(index, len));
	    Object[] newElements;
	    int numMoved = len - index;
	    if (numMoved == 0)
		newElements = Arrays.copyOf(es, len + 1);
	    else {
		newElements = new Object[len + 1];
		System.arraycopy(es, 0, newElements, 0, index);
		System.arraycopy(es, index, newElements, index + 1, numMoved);
	    }
	    newElements[index] = element;
	    setArray(newElements);
	}
    }

    /**
     * The lock protecting all mutators.  (We have a mild preference
     * for builtin monitors over ReentrantLock when either will do.)
     */
    final transient Object lock = new Object();
    /** The array, accessed only via getArray/setArray. */
    private transient volatile Object[] array;

    /**
     * Gets the array.  Non-private so as to also be accessible
     * from CopyOnWriteArraySet class.
     */
    final Object[] getArray() {
	return array;
    }

    static String outOfBounds(int index, int size) {
	return "Index: " + index + ", Size: " + size;
    }

    /**
     * Sets the array.
     */
    final void setArray(Object[] a) {
	array = a;
    }

}

