class LongHashMap {
    /**
     * Returns &lt;tt&gt;true&lt;/tt&gt; if this map contains no key-value mappings.
     *
     * @return &lt;tt&gt;true&lt;/tt&gt; if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
	return size == 0;
    }

    /**
     * The total number of mappings in the hash table.
     */
    transient int size;

}

