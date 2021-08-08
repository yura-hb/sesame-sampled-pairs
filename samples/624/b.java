import java.util.List;

class NameSpaceSymbTable {
    /**
     * Adds the mapping for a prefix.
     * @param prefix the prefix of definition
     * @param uri the Uri of the definition
     * @param n the attribute that have the definition
     * @return true if there is already defined.
     **/
    public boolean addMapping(String prefix, String uri, Attr n) {
	NameSpaceSymbEntry ob = symb.get(prefix);
	if (ob != null && uri.equals(ob.uri)) {
	    //If we have it previously defined. Don't keep working.
	    return false;
	}
	//Creates and entry in the table for this new definition.
	NameSpaceSymbEntry ne = new NameSpaceSymbEntry(uri, n, false, prefix);
	needsClone();
	symb.put(prefix, ne);
	if (ob != null) {
	    //We have a previous definition store it for the pop.
	    //Check if a previous definition(not the inmidiatly one) has been rendered.
	    ne.lastrendered = ob.lastrendered;
	    if (ob.lastrendered != null && ob.lastrendered.equals(uri)) {
		//Yes it is. Mark as rendered.
		ne.rendered = true;
	    }
	}
	return true;
    }

    /**The map betwen prefix-&gt; entry table. */
    private SymbMap symb;
    private boolean cloned = true;
    /**The stacks for removing the definitions when doing pop.*/
    private List&lt;SymbMap&gt; level;

    final void needsClone() {
	if (!cloned) {
	    level.set(level.size() - 1, symb);
	    symb = (SymbMap) symb.clone();
	    cloned = true;
	}
    }

}

class SymbMap implements Cloneable {
    NameSpaceSymbEntry get(String key) {
	return entries[index(key)];
    }

    NameSpaceSymbEntry[] entries;
    String[] keys;
    int free = 23;

    void put(String key, NameSpaceSymbEntry value) {
	int index = index(key);
	Object oldKey = keys[index];
	keys[index] = key;
	entries[index] = value;
	if ((oldKey == null || !oldKey.equals(key)) && --free == 0) {
	    free = entries.length;
	    int newCapacity = free &lt;&lt; 2;
	    rehash(newCapacity);
	}
    }

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

    protected Object clone() {
	try {
	    SymbMap copy = (SymbMap) super.clone();
	    copy.entries = new NameSpaceSymbEntry[entries.length];
	    System.arraycopy(entries, 0, copy.entries, 0, entries.length);
	    copy.keys = new String[keys.length];
	    System.arraycopy(keys, 0, copy.keys, 0, keys.length);

	    return copy;
	} catch (CloneNotSupportedException e) {
	    e.printStackTrace();
	}
	return null;
    }

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

}

class NameSpaceSymbEntry implements Cloneable {
    NameSpaceSymbEntry(String name, Attr n, boolean rendered, String prefix) {
	this.uri = name;
	this.rendered = rendered;
	this.n = n;
	this.prefix = prefix;
    }

    /**The URI that the prefix defines */
    String uri;
    /**The last output in the URI for this prefix (This for speed reason).*/
    String lastrendered = null;
    /**This prefix-URI has been already render or not.*/
    boolean rendered = false;
    /**The attribute to include.*/
    Attr n;
    String prefix;

}

