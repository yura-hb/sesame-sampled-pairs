import java.util.Arrays;
import java.util.Collection;

class CopyOnWriteArrayList&lt;E&gt; implements List&lt;E&gt;, RandomAccess, Cloneable, Serializable {
    /**
     * Appends all of the elements in the specified collection to the end
     * of this list, in the order that they are returned by the specified
     * collection's iterator.
     *
     * @param c collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     * @see #add(Object)
     */
    public boolean addAll(Collection&lt;? extends E&gt; c) {
	Object[] cs = (c.getClass() == CopyOnWriteArrayList.class) ? ((CopyOnWriteArrayList&lt;?&gt;) c).getArray()
		: c.toArray();
	if (cs.length == 0)
	    return false;
	synchronized (lock) {
	    Object[] es = getArray();
	    int len = es.length;
	    Object[] newElements;
	    if (len == 0 && cs.getClass() == Object[].class)
		newElements = cs;
	    else {
		newElements = Arrays.copyOf(es, len + cs.length);
		System.arraycopy(cs, 0, newElements, len, cs.length);
	    }
	    setArray(newElements);
	    return true;
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

    /**
     * Sets the array.
     */
    final void setArray(Object[] a) {
	array = a;
    }

}

