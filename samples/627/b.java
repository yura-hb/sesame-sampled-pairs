import java.util.AbstractMap;

abstract class AbstractPatriciaTrie&lt;K, V&gt; extends AbstractBitwiseTrie&lt;K, V&gt; {
    /**
     * Returns an entry strictly higher than the given key,
     * or null if no such entry exists.
     */
    TrieEntry&lt;K, V&gt; higherEntry(final K key) {
	// TODO: Cleanup so that we don't actually have to add/remove from the
	//       tree.  (We do it here because there are other well-defined
	//       functions to perform the search.)
	final int lengthInBits = lengthInBits(key);

	if (lengthInBits == 0) {
	    if (!root.isEmpty()) {
		// If data in root, and more after -- return it.
		if (size() &gt; 1) {
		    return nextEntry(root);
		}
		// If no more after, no higher entry.
		return null;
	    }
	    // Root is empty & we want something after empty, return first.
	    return firstEntry();
	}

	final TrieEntry&lt;K, V&gt; found = getNearestEntryForKey(key, lengthInBits);
	if (compareKeys(key, found.key)) {
	    return nextEntry(found);
	}

	final int bitIndex = bitIndex(key, found.key);
	if (KeyAnalyzer.isValidBitIndex(bitIndex)) {
	    final TrieEntry&lt;K, V&gt; added = new TrieEntry&lt;&gt;(key, null, bitIndex);
	    addEntry(added, lengthInBits);
	    incrementSize(); // must increment because remove will decrement
	    final TrieEntry&lt;K, V&gt; ceil = nextEntry(added);
	    removeEntry(added);
	    modCount -= 2; // we didn't really modify it.
	    return ceil;
	} else if (KeyAnalyzer.isNullBitKey(bitIndex)) {
	    if (!root.isEmpty()) {
		return firstEntry();
	    } else if (size() &gt; 1) {
		return nextEntry(firstEntry());
	    } else {
		return null;
	    }
	} else if (KeyAnalyzer.isEqualBitKey(bitIndex)) {
	    return nextEntry(found);
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

    @Override
    public int size() {
	return size;
    }

    /**
     * Returns the entry lexicographically after the given entry.
     * If the given entry is null, returns the first node.
     */
    TrieEntry&lt;K, V&gt; nextEntry(final TrieEntry&lt;K, V&gt; node) {
	if (node == null) {
	    return firstEntry();
	}
	return nextEntryImpl(node.predecessor, node, null);
    }

    /**
     * Returns the first entry the {@link Trie} is storing.
     * &lt;p&gt;
     * This is implemented by going always to the left until
     * we encounter a valid uplink. That uplink is the first key.
     */
    TrieEntry&lt;K, V&gt; firstEntry() {
	// if Trie is empty, no first node.
	if (isEmpty()) {
	    return null;
	}

	return followLeft(root);
    }

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
     * Scans for the next node, starting at the specified point, and using 'previous'
     * as a hint that the last node we returned was 'previous' (so we know not to return
     * it again).  If 'tree' is non-null, this will limit the search to the given tree.
     *
     * The basic premise is that each iteration can follow the following steps:
     *
     * 1) Scan all the way to the left.
     *   a) If we already started from this node last time, proceed to Step 2.
     *   b) If a valid uplink is found, use it.
     *   c) If the result is an empty node (root not set), break the scan.
     *   d) If we already returned the left node, break the scan.
     *
     * 2) Check the right.
     *   a) If we already returned the right node, proceed to Step 3.
     *   b) If it is a valid uplink, use it.
     *   c) Do Step 1 from the right node.
     *
     * 3) Back up through the parents until we encounter find a parent
     *    that we're not the right child of.
     *
     * 4) If there's no right child of that parent, the iteration is finished.
     *    Otherwise continue to Step 5.
     *
     * 5) Check to see if the right child is a valid uplink.
     *    a) If we already returned that child, proceed to Step 6.
     *       Otherwise, use it.
     *
     * 6) If the right child of the parent is the parent itself, we've
     *    already found & returned the end of the Trie, so exit.
     *
     * 7) Do Step 1 on the parent's right child.
     */
    TrieEntry&lt;K, V&gt; nextEntryImpl(final TrieEntry&lt;K, V&gt; start, final TrieEntry&lt;K, V&gt; previous,
	    final TrieEntry&lt;K, V&gt; tree) {

	TrieEntry&lt;K, V&gt; current = start;

	// Only look at the left if this was a recursive or
	// the first check, otherwise we know we've already looked
	// at the left.
	if (previous == null || start != previous.predecessor) {
	    while (!current.left.isEmpty()) {
		// stop traversing if we've already
		// returned the left of this node.
		if (previous == current.left) {
		    break;
		}

		if (isValidUplink(current.left, current)) {
		    return current.left;
		}

		current = current.left;
	    }
	}

	// If there's no data at all, exit.
	if (current.isEmpty()) {
	    return null;
	}

	// If we've already returned the left,
	// and the immediate right is null,
	// there's only one entry in the Trie
	// which is stored at the root.
	//
	//  / ("")   &lt;-- root
	//  \_/  \
	//       null &lt;-- 'current'
	//
	if (current.right == null) {
	    return null;
	}

	// If nothing valid on the left, try the right.
	if (previous != current.right) {
	    // See if it immediately is valid.
	    if (isValidUplink(current.right, current)) {
		return current.right;
	    }

	    // Must search on the right's side if it wasn't initially valid.
	    return nextEntryImpl(current.right, previous, tree);
	}

	// Neither left nor right are valid, find the first parent
	// whose child did not come from the right & traverse it.
	while (current == current.parent.right) {
	    // If we're going to traverse to above the subtree, stop.
	    if (current == tree) {
		return null;
	    }

	    current = current.parent;
	}

	// If we're on the top of the subtree, we can't go any higher.
	if (current == tree) {
	    return null;
	}

	// If there's no right, the parent must be root, so we're done.
	if (current.parent.right == null) {
	    return null;
	}

	// If the parent's right points to itself, we've found one.
	if (previous != current.parent.right && isValidUplink(current.parent.right, current.parent)) {
	    return current.parent.right;
	}

	// If the parent's right is itself, there can't be any more nodes.
	if (current.parent.right == current.parent) {
	    return null;
	}

	// We need to traverse down the parent's right's path.
	return nextEntryImpl(current.parent.right, previous, tree);
    }

    /**
     * Goes left through the tree until it finds a valid node.
     */
    TrieEntry&lt;K, V&gt; followLeft(TrieEntry&lt;K, V&gt; node) {
	while (true) {
	    TrieEntry&lt;K, V&gt; child = node.left;
	    // if we hit root and it didn't have a node, go right instead.
	    if (child.isEmpty()) {
		child = node.right;
	    }

	    if (child.bitIndex &lt;= node.bitIndex) {
		return child;
	    }

	    node = child;
	}
    }

    /**
     * A helper method to increment the modification counter.
     */
    private void incrementModCount() {
	++modCount;
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

    /**
     * Returns true if 'next' is a valid uplink coming from 'from'.
     */
    static boolean isValidUplink(final TrieEntry&lt;?, ?&gt; next, final TrieEntry&lt;?, ?&gt; from) {
	return next != null && next.bitIndex &lt;= from.bitIndex && !next.isEmpty();
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

