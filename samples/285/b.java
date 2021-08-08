class LongHashMap {
    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or &lt;tt&gt;null&lt;/tt&gt;
     *         if there was no mapping for key.  A &lt;tt&gt;null&lt;/tt&gt; return can
     *         also indicate that the HashMap previously associated
     *         &lt;tt&gt;null&lt;/tt&gt; with the specified key.
     */
    public Object put(long key, Object value) {
	Entry tab[] = table;
	int hash = (int) key;
	int index = (hash & 0x7FFFFFFF) % tab.length;

	// Look for entry in hash table
	for (Entry e = tab[index]; e != null; e = e.next) {
	    if (e.hash == hash && e.key == key) {
		Object oldValue = e.value;
		e.value = value;
		return oldValue;
	    }
	}

	// It's not there; grow the hash table if necessary...
	modCount++;
	if (size &gt;= threshold) {
	    rehash();
	    tab = table;
	    index = (hash & 0x7FFFFFFF) % tab.length;
	}

	// ...and add the entry
	size++;
	tab[index] = newEntry(hash, key, value, tab[index]);
	return null;
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
    /**
     * The table is rehashed when its size exceeds this threshold.  (The
     * value of this field is (int)(capacity * loadFactor).)
     *
     * @serial
     */
    int threshold;
    /**
     * The load factor for the hash table.
     *
     * @serial
     */
    final float loadFactor;

    /**
     * Rehashes the contents of this map into a new &lt;tt&gt;HashMap&lt;/tt&gt; instance
     * with a larger capacity. This method is called automatically when the
     * number of keys in this map exceeds its capacity and load factor.
     */
    void rehash() {
	Entry oldTable[] = table;
	int oldCapacity = oldTable.length;
	int newCapacity = oldCapacity * 2 + 1;
	Entry newTable[] = new Entry[newCapacity];

	modCount++;
	threshold = (int) (newCapacity * loadFactor);
	table = newTable;

	for (int i = oldCapacity; i-- &gt; 0;) {
	    for (Entry old = oldTable[i]; old != null;) {
		Entry e = old;
		old = old.next;

		int index = (e.hash & 0x7FFFFFFF) % newCapacity;
		e.next = newTable[index];
		newTable[index] = e;
	    }
	}
    }

    Entry newEntry(int hash, long key, Object value, Entry next) {
	return new Entry(hash, key, value, next);
    }

    class Entry {
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
	/**
	* The table is rehashed when its size exceeds this threshold.  (The
	* value of this field is (int)(capacity * loadFactor).)
	*
	* @serial
	*/
	int threshold;
	/**
	* The load factor for the hash table.
	*
	* @serial
	*/
	final float loadFactor;

	Entry(int hash, long key, Object value, Entry next) {
	    this.hash = hash;
	    this.key = key;
	    this.value = value;
	    this.next = next;
	}

    }

}

