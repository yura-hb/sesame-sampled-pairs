class LongHashMap {
    /**
     * Removes all mappings from this map.
     */
    public void clear() {
	Entry tab[] = table;
	modCount++;
	for (int index = tab.length; --index &gt;= 0;)
	    tab[index] = null;
	size = 0;
    }

    /**
     * The hash table data.
     */
    transient Entry table[];
    /**
     * The number of times this HashMap has been structurally modified
     * Structural modifications are those that change the number of mappings in
     * the HashMap or otherwise modify its internal structure (e.g.,
     * rehash).  This field is used to make iterators on Collection-views of
     * the HashMap fail-fast.  (See ConcurrentModificationException).
     */
    transient int modCount = 0;
    /**
     * The total number of mappings in the hash table.
     */
    transient int size;

}

