class ConcurrentHashMapV7&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  The set supports element
     * removal, which removes the corresponding mapping from this map,
     * via the &lt;tt&gt;Iterator.remove&lt;/tt&gt;, &lt;tt&gt;Set.remove&lt;/tt&gt;,
     * &lt;tt&gt;removeAll&lt;/tt&gt;, &lt;tt&gt;retainAll&lt;/tt&gt;, and &lt;tt&gt;clear&lt;/tt&gt;
     * operations.  It does not support the &lt;tt&gt;add&lt;/tt&gt; or
     * &lt;tt&gt;addAll&lt;/tt&gt; operations.
     *
     * &lt;p&gt;The view's &lt;tt&gt;iterator&lt;/tt&gt; is a "weakly consistent" iterator
     * that will never throw {@link ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to)
     * reflect any modifications subsequent to construction.
     */
    @Override
    public Set&lt;K&gt; keySet() {
	Set&lt;K&gt; ks = keySet;
	return (ks != null) ? ks : (keySet = new KeySet());
    }

    transient Set&lt;K&gt; keySet;

}

