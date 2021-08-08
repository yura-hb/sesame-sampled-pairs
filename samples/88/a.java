abstract class AbstractPatriciaTrie&lt;K, V&gt; extends AbstractBitwiseTrie&lt;K, V&gt; {
    /**
     * Returns a key-value mapping associated with the greatest key
     * less than or equal to the given key, or null if there is no such key.
     */
    TrieEntry&lt;K, V&gt; floorEntry(final K key) {
	// TODO: Cleanup so that we don't actually have to add/remove from the
	//       tree.  (We do it here because there are other well-defined
	//       functions to perform the search.)
	final int lengthInBits = lengthInBits(key);

	if (lengthInBits == 0) {
	    if (!root.isEmpty()) {
		return root;
	    }
	    return null;
	}

	final TrieEntry&lt;K, V&gt; found = getNearestEntryForKey(key, lengthInBits);
	if (compareKeys(key, found.key)) {
	    return found;
	}

	final int bitIndex = bitIndex(key, found.key);
	if (KeyAnalyzer.isValidBitIndex(bitIndex)) {
	    final TrieEntry&lt;K, V&gt; added = new TrieEntry&lt;&gt;(key, null, bitIndex);
	    addEntry(added, lengthInBits);
	    incrementSize(); // must increment because remove will decrement
	    final TrieEntry&lt;K, V&gt; floor = previousEntry(added);
	    removeEntry(added);
	    modCount -= 2; // we didn't really modify it.
	    return floor;
	} else if (KeyAnalyzer.isNullBitKey(bitIndex)) {
	    if (!root.isEmpty()) {
		return root;
	    }
	    return null;
	} else if (KeyAnalyzer.isEqualBitKey(bitIndex)) {
	    return found;
	}

	// we should have exited above.
	throw new IllegalStateException("invalid lookup: " + key);
    }

    /** The root node of the {@link org.apache.commons.collections4.Trie}. */
    private transient TrieEntry&lt;K, V&gt; root = new TrieEntry&lt;&gt;(null, null, -1);
    /**
     * The number of times this {@link org.apache.commons.collections4.Trie} has been modified.
     * It's used to detect concurrent modifications and fail-fast the {@link Iterator}s.
     */
    protected transient int modCount = 0;
    /** The current size of the {@link org.apache.commons.collections4.Trie}. */
    private transient int size = 0;

    /**
     * Returns the nearest entry for a given key.  This is useful
     * for finding knowing if a given key exists (and finding the value
     * for it), or for inserting the key.
     *
     * The actual get implementation. This is very similar to
     * selectR but with the exception that it might return the
     * root Entry even if it's empty.
     */
    TrieEntry&lt;K, V&gt; getNearestEntryForKey(final K key, final int lengthInBits) {
	TrieEntry&lt;K, V&gt; current = root.left;
	TrieEntry&lt;K, V&gt; path = root;
	while (true) {
	    if (current.bitIndex &lt;= path.bitIndex) {
		return current;
	    }

	    path = current;
	    if (!isBitSet(key, current.bitIndex, lengthInBits)) {
		current = current.left;
	    } else {
		current = current.right;
	    }
	}
    }

    /**
     * Adds the given {@link TrieEntry} to the {@link Trie}.
     */
    TrieEntry&lt;K, V&gt; addEntry(final TrieEntry&lt;K, V&gt; entry, final int lengthInBits) {
	TrieEntry&lt;K, V&gt; current = root.left;
	TrieEntry&lt;K, V&gt; path = root;
	while (true) {
	    if (current.bitIndex &gt;= entry.bitIndex || current.bitIndex &lt;= path.bitIndex) {
		entry.predecessor = entry;

		if (!isBitSet(entry.key, entry.bitIndex, lengthInBits)) {
		    entry.left = entry;
		    entry.right = current;
		} else {
		    entry.left = current;
		    entry.right = entry;
		}

		entry.parent = path;
		if (current.bitIndex &gt;= entry.bitIndex) {
		    current.parent = entry;
		}

		// if we inserted an uplink, set the predecessor on it
		if (current.bitIndex &lt;= path.bitIndex) {
		    current.predecessor = entry;
		}

		if (path == root || !isBitSet(entry.key, path.bitIndex, lengthInBits)) {
		    path.left = entry;
		} else {
		    path.right = entry;
		}

		return entry;
	    }

	    path = current;

	    if (!isBitSet(entry.key, current.bitIndex, lengthInBits)) {
		current = current.left;
	    } else {
		current = current.right;
	    }
	}
    }

    /**
     * A helper method to increment the {@link Trie} size and the modification counter.
     */
    void incrementSize() {
	size++;
	incrementModCount();
    }

    /**
     * Returns the node lexicographically before the given node (or null if none).
     *
     * This follows four simple branches:
     *  - If the uplink that returned us was a right uplink:
     *      - If predecessor's left is a valid uplink from predecessor, return it.
     *      - Else, follow the right path from the predecessor's left.
     *  - If the uplink that returned us was a left uplink:
     *      - Loop back through parents until we encounter a node where
     *        node != node.parent.left.
     *          - If node.parent.left is uplink from node.parent:
     *              - If node.parent.left is not root, return it.
     *              - If it is root & root isEmpty, return null.
     *              - If it is root & root !isEmpty, return root.
     *          - If node.parent.left is not uplink from node.parent:
     *              - Follow right path for first right child from node.parent.left
     *
     * @param start  the start entry
     */
    TrieEntry&lt;K, V&gt; previousEntry(final TrieEntry&lt;K, V&gt; start) {
	if (start.predecessor == null) {
	    throw new IllegalArgumentException("must have come from somewhere!");
	}

	if (start.predecessor.right == start) {
	    if (isValidUplink(start.predecessor.left, start.predecessor)) {
		return start.predecessor.left;
	    }
	    return followRight(start.predecessor.left);
	}
	TrieEntry&lt;K, V&gt; node = start.predecessor;
	while (node.parent != null && node == node.parent.left) {
	    node = node.parent;
	}

	if (node.parent == null) { // can be null if we're looking up root.
	    return null;
	}

	if (isValidUplink(node.parent.left, node.parent)) {
	    if (node.parent.left == root) {
		if (root.isEmpty()) {
		    return null;
		}
		return root;

	    }
	    return node.parent.left;
	}
	return followRight(node.parent.left);
    }

    /**
     * Removes a single entry from the {@link Trie}.
     *
     * If we found a Key (Entry h) then figure out if it's
     * an internal (hard to remove) or external Entry (easy
     * to remove)
     */
    V removeEntry(final TrieEntry&lt;K, V&gt; h) {
	if (h != root) {
	    if (h.isInternalNode()) {
		removeInternalEntry(h);
	    } else {
		removeExternalEntry(h);
	    }
	}

	decrementSize();
	return h.setKeyValue(null, null);
    }

    /**
     * A helper method to increment the modification counter.
     */
    private void incrementModCount() {
	++modCount;
    }

    /**
     * Returns true if 'next' is a valid uplink coming from 'from'.
     */
    static boolean isValidUplink(final TrieEntry&lt;?, ?&gt; next, final TrieEntry&lt;?, ?&gt; from) {
	return next != null && next.bitIndex &lt;= from.bitIndex && !next.isEmpty();
    }

    /**
     * Traverses down the right path until it finds an uplink.
     */
    TrieEntry&lt;K, V&gt; followRight(TrieEntry&lt;K, V&gt; node) {
	// if Trie is empty, no last entry.
	if (node.right == null) {
	    return null;
	}

	// Go as far right as possible, until we encounter an uplink.
	while (node.right.bitIndex &gt; node.bitIndex) {
	    node = node.right;
	}

	return node.right;
    }

    /**
     * Removes an internal entry from the {@link Trie}.
     *
     * If it's an internal Entry then "good luck" with understanding
     * this code. The Idea is essentially that Entry p takes Entry h's
     * place in the trie which requires some re-wiring.
     */
    private void removeInternalEntry(final TrieEntry&lt;K, V&gt; h) {
	if (h == root) {
	    throw new IllegalArgumentException("Cannot delete root Entry!");
	} else if (!h.isInternalNode()) {
	    throw new IllegalArgumentException(h + " is not an internal Entry!");
	}

	final TrieEntry&lt;K, V&gt; p = h.predecessor;

	// Set P's bitIndex
	p.bitIndex = h.bitIndex;

	// Fix P's parent, predecessor and child Nodes
	{
	    final TrieEntry&lt;K, V&gt; parent = p.parent;
	    final TrieEntry&lt;K, V&gt; child = p.left == h ? p.right : p.left;

	    // if it was looping to itself previously,
	    // it will now be pointed from it's parent
	    // (if we aren't removing it's parent --
	    //  in that case, it remains looping to itself).
	    // otherwise, it will continue to have the same
	    // predecessor.
	    if (p.predecessor == p && p.parent != h) {
		p.predecessor = p.parent;
	    }

	    if (parent.left == p) {
		parent.left = child;
	    } else {
		parent.right = child;
	    }

	    if (child.bitIndex &gt; parent.bitIndex) {
		child.parent = parent;
	    }
	}

	// Fix H's parent and child Nodes
	{
	    // If H is a parent of its left and right child
	    // then change them to P
	    if (h.left.parent == h) {
		h.left.parent = p;
	    }

	    if (h.right.parent == h) {
		h.right.parent = p;
	    }

	    // Change H's parent
	    if (h.parent.left == h) {
		h.parent.left = p;
	    } else {
		h.parent.right = p;
	    }
	}

	// Copy the remaining fields from H to P
	//p.bitIndex = h.bitIndex;
	p.parent = h.parent;
	p.left = h.left;
	p.right = h.right;

	// Make sure that if h was pointing to any uplinks,
	// p now points to them.
	if (isValidUplink(p.left, p)) {
	    p.left.predecessor = p;
	}

	if (isValidUplink(p.right, p)) {
	    p.right.predecessor = p;
	}
    }

    /**
     * Removes an external entry from the {@link Trie}.
     *
     * If it's an external Entry then just remove it.
     * This is very easy and straight forward.
     */
    private void removeExternalEntry(final TrieEntry&lt;K, V&gt; h) {
	if (h == root) {
	    throw new IllegalArgumentException("Cannot delete root Entry!");
	} else if (!h.isExternalNode()) {
	    throw new IllegalArgumentException(h + " is not an external Entry!");
	}

	final TrieEntry&lt;K, V&gt; parent = h.parent;
	final TrieEntry&lt;K, V&gt; child = h.left == h ? h.right : h.left;

	if (parent.left == h) {
	    parent.left = child;
	} else {
	    parent.right = child;
	}

	// either the parent is changing, or the predecessor is changing.
	if (child.bitIndex &gt; parent.bitIndex) {
	    child.parent = parent;
	} else {
	    child.predecessor = parent;
	}

    }

    /**
     * A helper method to decrement the {@link Trie} size and increment the modification counter.
     */
    void decrementSize() {
	size--;
	incrementModCount();
    }

    class TrieEntry&lt;K, V&gt; extends BasicEntry&lt;K, V&gt; {
	/** The root node of the {@link org.apache.commons.collections4.Trie}. */
	private transient TrieEntry&lt;K, V&gt; root = new TrieEntry&lt;&gt;(null, null, -1);
	/**
	* The number of times this {@link org.apache.commons.collections4.Trie} has been modified.
	* It's used to detect concurrent modifications and fail-fast the {@link Iterator}s.
	*/
	protected transient int modCount = 0;
	/** The current size of the {@link org.apache.commons.collections4.Trie}. */
	private transient int size = 0;

	/**
	 * Whether or not the entry is storing a key.
	 * Only the root can potentially be empty, all other
	 * nodes must have a key.
	 */
	public boolean isEmpty() {
	    return key == null;
	}

	public TrieEntry(final K key, final V value, final int bitIndex) {
	    super(key, value);

	    this.bitIndex = bitIndex;

	    this.parent = null;
	    this.left = this;
	    this.right = null;
	    this.predecessor = this;
	}

	/**
	 * Neither the left nor right child is a loopback.
	 */
	public boolean isInternalNode() {
	    return left != this && right != this;
	}

	/**
	 * Either the left or right child is a loopback.
	 */
	public boolean isExternalNode() {
	    return !isInternalNode();
	}

    }

}

