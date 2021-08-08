class IdentityHashMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements Map&lt;K, V&gt;, Serializable, Cloneable {
    /**
     * Returns a shallow copy of this identity hash map: the keys and values
     * themselves are not cloned.
     *
     * @return a shallow copy of this map
     */
    public Object clone() {
	try {
	    IdentityHashMap&lt;?, ?&gt; m = (IdentityHashMap&lt;?, ?&gt;) super.clone();
	    m.entrySet = null;
	    m.table = table.clone();
	    return m;
	} catch (CloneNotSupportedException e) {
	    throw new InternalError(e);
	}
    }

    /**
     * This field is initialized to contain an instance of the entry set
     * view the first time this view is requested.  The view is stateless,
     * so there's no reason to create more than one.
     */
    private transient Set&lt;Map.Entry&lt;K, V&gt;&gt; entrySet;
    /**
     * The table, resized as necessary. Length MUST always be a power of two.
     */
    transient Object[] table;

}

