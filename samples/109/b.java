class TreeMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements NavigableMap&lt;K, V&gt;, Cloneable, Serializable {
    /**
     * Returns {@code true} if this map maps one or more keys to the
     * specified value.  More formally, returns {@code true} if and only if
     * this map contains at least one mapping to a value {@code v} such
     * that {@code (value==null ? v==null : value.equals(v))}.  This
     * operation will probably require time linear in the map size for
     * most implementations.
     *
     * @param value value whose presence in this map is to be tested
     * @return {@code true} if a mapping to {@code value} exists;
     *         {@code false} otherwise
     * @since 1.2
     */
    public boolean containsValue(Object value) {
	for (Entry&lt;K, V&gt; e = getFirstEntry(); e != null; e = successor(e))
	    if (valEquals(value, e.value))
		return true;
	return false;
    }

    private transient Entry&lt;K, V&gt; root;

    /**
     * Returns the first Entry in the TreeMap (according to the TreeMap's
     * key-sort function).  Returns null if the TreeMap is empty.
     */
    final Entry&lt;K, V&gt; getFirstEntry() {
	Entry&lt;K, V&gt; p = root;
	if (p != null)
	    while (p.left != null)
		p = p.left;
	return p;
    }

    /**
     * Returns the successor of the specified Entry, or null if no such.
     */
    static &lt;K, V&gt; TreeMap.Entry&lt;K, V&gt; successor(Entry&lt;K, V&gt; t) {
	if (t == null)
	    return null;
	else if (t.right != null) {
	    Entry&lt;K, V&gt; p = t.right;
	    while (p.left != null)
		p = p.left;
	    return p;
	} else {
	    Entry&lt;K, V&gt; p = t.parent;
	    Entry&lt;K, V&gt; ch = t;
	    while (p != null && ch == p.right) {
		ch = p;
		p = p.parent;
	    }
	    return p;
	}
    }

    /**
     * Test two values for equality.  Differs from o1.equals(o2) only in
     * that it copes with {@code null} o1 properly.
     */
    static final boolean valEquals(Object o1, Object o2) {
	return (o1 == null ? o2 == null : o1.equals(o2));
    }

}

