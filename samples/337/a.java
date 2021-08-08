class SymbMap implements Cloneable {
    /**
     * rehashes the map to the new capacity.
     *
     * @param newCapacity an {@code int} value
     */
    protected void rehash(int newCapacity) {
	int oldCapacity = keys.length;
	String oldKeys[] = keys;
	NameSpaceSymbEntry oldVals[] = entries;

	keys = new String[newCapacity];
	entries = new NameSpaceSymbEntry[newCapacity];

	for (int i = oldCapacity; i-- &gt; 0;) {
	    if (oldKeys[i] != null) {
		String o = oldKeys[i];
		int index = index(o);
		keys[index] = o;
		entries[index] = oldVals[i];
	    }
	}
    }

    String[] keys;
    NameSpaceSymbEntry[] entries;

    protected int index(Object obj) {
	Object[] set = keys;
	int length = set.length;
	//abs of index
	int index = (obj.hashCode() & 0x7fffffff) % length;
	Object cur = set[index];

	if (cur == null || cur.equals(obj)) {
	    return index;
	}
	length--;
	do {
	    index = index == length ? 0 : ++index;
	    cur = set[index];
	} while (cur != null && !cur.equals(obj));
	return index;
    }

}

