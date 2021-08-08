abstract class THash implements Externalizable {
    /**
     * Tells whether this set is currently holding any elements.
     *
     * @return a &lt;code&gt;boolean&lt;/code&gt; value
     */
    public boolean isEmpty() {
	return 0 == _size;
    }

    /** the current number of occupied slots in the hash. */
    protected transient int _size;

}

