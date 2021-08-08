class ConcurrentHashMapV7&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    /**
     * Sets the ith element of given table, with volatile write
     * semantics. (See above about use of putOrderedObject.)
     */
    static final &lt;K, V&gt; void setEntryAt(HashEntry&lt;K, V&gt;[] tab, int i, HashEntry&lt;K, V&gt; e) {
	UNSAFE.putOrderedObject(tab, ((long) i &lt;&lt; TSHIFT) + TBASE, e);
    }

    private static final sun.misc.Unsafe UNSAFE;
    private static final int TSHIFT;
    private static final long TBASE;

}

