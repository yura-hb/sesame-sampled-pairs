class CopyOnWriteArrayList&lt;E&gt; implements List&lt;E&gt;, RandomAccess, Cloneable, Serializable {
    /**
     * Removes all of the elements from this list.
     * The list will be empty after this call returns.
     */
    public void clear() {
	synchronized (lock) {
	    setArray(new Object[0]);
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
     * Sets the array.
     */
    final void setArray(Object[] a) {
	array = a;
    }

}

