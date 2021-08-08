class TreeMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements NavigableMap&lt;K, V&gt;, Cloneable, Serializable {
    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings replace any mappings that this map had for any
     * of the keys currently in the specified map.
     *
     * @param  map mappings to be stored in this map
     * @throws ClassCastException if the class of a key or value in
     *         the specified map prevents it from being stored in this map
     * @throws NullPointerException if the specified map is null or
     *         the specified map contains a null key and this map does not
     *         permit null keys
     */
    public void putAll(Map&lt;? extends K, ? extends V&gt; map) {
	int mapSize = map.size();
	if (size == 0 && mapSize != 0 && map instanceof SortedMap) {
	    Comparator&lt;?&gt; c = ((SortedMap&lt;?, ?&gt;) map).comparator();
	    if (c == comparator || (c != null && c.equals(comparator))) {
		++modCount;
		try {
		    buildFromSorted(mapSize, map.entrySet().iterator(), null, null);
		} catch (java.io.IOException | ClassNotFoundException cannotHappen) {
		}
		return;
	    }
	}
	super.putAll(map);
    }

    /**
     * The number of entries in the tree
     */
    private transient int size = 0;
    /**
     * The comparator used to maintain order in this tree map, or
     * null if it uses the natural ordering of its keys.
     *
     * @serial
     */
    private final Comparator&lt;? super K&gt; comparator;
    /**
     * The number of structural modifications to the tree.
     */
    private transient int modCount = 0;
    private transient Entry&lt;K, V&gt; root;
    private static final boolean RED = false;

    /**
     * Linear time tree building algorithm from sorted data.  Can accept keys
     * and/or values from iterator or stream. This leads to too many
     * parameters, but seems better than alternatives.  The four formats
     * that this method accepts are:
     *
     *    1) An iterator of Map.Entries.  (it != null, defaultVal == null).
     *    2) An iterator of keys.         (it != null, defaultVal != null).
     *    3) A stream of alternating serialized keys and values.
     *                                   (it == null, defaultVal == null).
     *    4) A stream of serialized keys. (it == null, defaultVal != null).
     *
     * It is assumed that the comparator of the TreeMap is already set prior
     * to calling this method.
     *
     * @param size the number of keys (or key-value pairs) to be read from
     *        the iterator or stream
     * @param it If non-null, new entries are created from entries
     *        or keys read from this iterator.
     * @param str If non-null, new entries are created from keys and
     *        possibly values read from this stream in serialized form.
     *        Exactly one of it and str should be non-null.
     * @param defaultVal if non-null, this default value is used for
     *        each value in the map.  If null, each value is read from
     *        iterator or stream, as described above.
     * @throws java.io.IOException propagated from stream reads. This cannot
     *         occur if str is null.
     * @throws ClassNotFoundException propagated from readObject.
     *         This cannot occur if str is null.
     */
    private void buildFromSorted(int size, Iterator&lt;?&gt; it, java.io.ObjectInputStream str, V defaultVal)
	    throws java.io.IOException, ClassNotFoundException {
	this.size = size;
	root = buildFromSorted(0, 0, size - 1, computeRedLevel(size), it, str, defaultVal);
    }

    /**
     * Finds the level down to which to assign all nodes BLACK.  This is the
     * last `full' level of the complete binary tree produced by buildTree.
     * The remaining nodes are colored RED. (This makes a `nice' set of
     * color assignments wrt future insertions.) This level number is
     * computed by finding the number of splits needed to reach the zeroeth
     * node.
     *
     * @param size the (non-negative) number of keys in the tree to be built
     */
    private static int computeRedLevel(int size) {
	return 31 - Integer.numberOfLeadingZeros(size + 1);
    }

    /**
     * Recursive "helper method" that does the real work of the
     * previous method.  Identically named parameters have
     * identical definitions.  Additional parameters are documented below.
     * It is assumed that the comparator and size fields of the TreeMap are
     * already set prior to calling this method.  (It ignores both fields.)
     *
     * @param level the current level of tree. Initial call should be 0.
     * @param lo the first element index of this subtree. Initial should be 0.
     * @param hi the last element index of this subtree.  Initial should be
     *        size-1.
     * @param redLevel the level at which nodes should be red.
     *        Must be equal to computeRedLevel for tree of this size.
     */
    @SuppressWarnings("unchecked")
    private final Entry&lt;K, V&gt; buildFromSorted(int level, int lo, int hi, int redLevel, Iterator&lt;?&gt; it,
	    java.io.ObjectInputStream str, V defaultVal) throws java.io.IOException, ClassNotFoundException {
	/*
	 * Strategy: The root is the middlemost element. To get to it, we
	 * have to first recursively construct the entire left subtree,
	 * so as to grab all of its elements. We can then proceed with right
	 * subtree.
	 *
	 * The lo and hi arguments are the minimum and maximum
	 * indices to pull out of the iterator or stream for current subtree.
	 * They are not actually indexed, we just proceed sequentially,
	 * ensuring that items are extracted in corresponding order.
	 */

	if (hi &lt; lo)
	    return null;

	int mid = (lo + hi) &gt;&gt;&gt; 1;

	Entry&lt;K, V&gt; left = null;
	if (lo &lt; mid)
	    left = buildFromSorted(level + 1, lo, mid - 1, redLevel, it, str, defaultVal);

	// extract key and/or value from iterator or stream
	K key;
	V value;
	if (it != null) {
	    if (defaultVal == null) {
		Map.Entry&lt;?, ?&gt; entry = (Map.Entry&lt;?, ?&gt;) it.next();
		key = (K) entry.getKey();
		value = (V) entry.getValue();
	    } else {
		key = (K) it.next();
		value = defaultVal;
	    }
	} else { // use stream
	    key = (K) str.readObject();
	    value = (defaultVal != null ? defaultVal : (V) str.readObject());
	}

	Entry&lt;K, V&gt; middle = new Entry&lt;&gt;(key, value, null);

	// color nodes in non-full bottommost level red
	if (level == redLevel)
	    middle.color = RED;

	if (left != null) {
	    middle.left = left;
	    left.parent = middle;
	}

	if (mid &lt; hi) {
	    Entry&lt;K, V&gt; right = buildFromSorted(level + 1, mid + 1, hi, redLevel, it, str, defaultVal);
	    middle.right = right;
	    right.parent = middle;
	}

	return middle;
    }

    class Entry&lt;K, V&gt; implements Entry&lt;K, V&gt; {
	/**
	* The number of entries in the tree
	*/
	private transient int size = 0;
	/**
	* The comparator used to maintain order in this tree map, or
	* null if it uses the natural ordering of its keys.
	*
	* @serial
	*/
	private final Comparator&lt;? super K&gt; comparator;
	/**
	* The number of structural modifications to the tree.
	*/
	private transient int modCount = 0;
	private transient Entry&lt;K, V&gt; root;
	private static final boolean RED = false;

	/**
	 * Make a new cell with given key, value, and parent, and with
	 * {@code null} child links, and BLACK color.
	 */
	Entry(K key, V value, Entry&lt;K, V&gt; parent) {
	    this.key = key;
	    this.value = value;
	    this.parent = parent;
	}

    }

}

