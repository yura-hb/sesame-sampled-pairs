class TreeList&lt;E&gt; extends AbstractList&lt;E&gt; {
    class AVLNode&lt;E&gt; {
	/**
	 * Gets the node in the list before this one.
	 *
	 * @return the previous node
	 */
	AVLNode&lt;E&gt; previous() {
	    if (leftIsPrevious || left == null) {
		return left;
	    }
	    return left.max();
	}

	/** Flag indicating that left reference is not a subtree but the predecessor. */
	private boolean leftIsPrevious;
	/** The left child node or the predecessor if {@link #leftIsPrevious}.*/
	private AVLNode&lt;E&gt; left;
	/** The right child node or the successor if {@link #rightIsNext}. */
	private AVLNode&lt;E&gt; right;
	/** Flag indicating that right reference is not a subtree but the successor. */
	private boolean rightIsNext;

	/**
	 * Gets the rightmost child of this node.
	 *
	 * @return the rightmost child (greatest index)
	 */
	private AVLNode&lt;E&gt; max() {
	    return getRightSubTree() == null ? this : right.max();
	}

	/**
	 * Gets the right node, returning null if its a faedelung.
	 */
	private AVLNode&lt;E&gt; getRightSubTree() {
	    return rightIsNext ? null : right;
	}

    }

}

