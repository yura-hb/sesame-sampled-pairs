import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

class TreeList&lt;E&gt; extends AbstractList&lt;E&gt; {
    /**
     * Appends all of the elements in the specified collection to the end of this list,
     * in the order that they are returned by the specified collection's Iterator.
     * &lt;p&gt;
     * This method runs in O(n + log m) time, where m is
     * the size of this list and n is the size of {@code c}.
     *
     * @param c  the collection to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public boolean addAll(final Collection&lt;? extends E&gt; c) {
	if (c.isEmpty()) {
	    return false;
	}
	modCount += c.size();
	final AVLNode&lt;E&gt; cTree = new AVLNode&lt;&gt;(c);
	root = root == null ? cTree : root.addAll(cTree, size);
	size += c.size();
	return true;
    }

    /** The root node in the AVL tree */
    private AVLNode&lt;E&gt; root;
    /** The current size of the list */
    private int size;

    class AVLNode&lt;E&gt; {
	/** The root node in the AVL tree */
	private AVLNode&lt;E&gt; root;
	/** The current size of the list */
	private int size;

	/**
	 * Constructs a new AVL tree from a collection.
	 * &lt;p&gt;
	 * The collection must be nonempty.
	 *
	 * @param coll  a nonempty collection
	 */
	private AVLNode(final Collection&lt;? extends E&gt; coll) {
	    this(coll.iterator(), 0, coll.size() - 1, 0, null, null);
	}

	/**
	 * Appends the elements of another tree list to this tree list by efficiently
	 * merging the two AVL trees. This operation is destructive to both trees and
	 * runs in O(log(m + n)) time.
	 *
	 * @param otherTree
	 *            the root of the AVL tree to merge with this one
	 * @param currentSize
	 *            the number of elements in this AVL tree
	 * @return the root of the new, merged AVL tree
	 */
	private AVLNode&lt;E&gt; addAll(AVLNode&lt;E&gt; otherTree, final int currentSize) {
	    final AVLNode&lt;E&gt; maxNode = max();
	    final AVLNode&lt;E&gt; otherTreeMin = otherTree.min();

	    // We need to efficiently merge the two AVL trees while keeping them
	    // balanced (or nearly balanced). To do this, we take the shorter
	    // tree and combine it with a similar-height subtree of the taller
	    // tree. There are two symmetric cases:
	    //   * this tree is taller, or
	    //   * otherTree is taller.
	    if (otherTree.height &gt; height) {
		// CASE 1: The other tree is taller than this one. We will thus
		// merge this tree into otherTree.

		// STEP 1: Remove the maximum element from this tree.
		final AVLNode&lt;E&gt; leftSubTree = removeMax();

		// STEP 2: Navigate left from the root of otherTree until we
		// find a subtree, s, that is no taller than me. (While we are
		// navigating left, we store the nodes we encounter in a stack
		// so that we can re-balance them in step 4.)
		final Deque&lt;AVLNode&lt;E&gt;&gt; sAncestors = new ArrayDeque&lt;&gt;();
		AVLNode&lt;E&gt; s = otherTree;
		int sAbsolutePosition = s.relativePosition + currentSize;
		int sParentAbsolutePosition = 0;
		while (s != null && s.height &gt; getHeight(leftSubTree)) {
		    sParentAbsolutePosition = sAbsolutePosition;
		    sAncestors.push(s);
		    s = s.left;
		    if (s != null) {
			sAbsolutePosition += s.relativePosition;
		    }
		}

		// STEP 3: Replace s with a newly constructed subtree whose root
		// is maxNode, whose left subtree is leftSubTree, and whose right
		// subtree is s.
		maxNode.setLeft(leftSubTree, null);
		maxNode.setRight(s, otherTreeMin);
		if (leftSubTree != null) {
		    leftSubTree.max().setRight(null, maxNode);
		    leftSubTree.relativePosition -= currentSize - 1;
		}
		if (s != null) {
		    s.min().setLeft(null, maxNode);
		    s.relativePosition = sAbsolutePosition - currentSize + 1;
		}
		maxNode.relativePosition = currentSize - 1 - sParentAbsolutePosition;
		otherTree.relativePosition += currentSize;

		// STEP 4: Re-balance the tree and recalculate the heights of s's ancestors.
		s = maxNode;
		while (!sAncestors.isEmpty()) {
		    final AVLNode&lt;E&gt; sAncestor = sAncestors.pop();
		    sAncestor.setLeft(s, null);
		    s = sAncestor.balance();
		}
		return s;
	    }
	    otherTree = otherTree.removeMin();

	    final Deque&lt;AVLNode&lt;E&gt;&gt; sAncestors = new ArrayDeque&lt;&gt;();
	    AVLNode&lt;E&gt; s = this;
	    int sAbsolutePosition = s.relativePosition;
	    int sParentAbsolutePosition = 0;
	    while (s != null && s.height &gt; getHeight(otherTree)) {
		sParentAbsolutePosition = sAbsolutePosition;
		sAncestors.push(s);
		s = s.right;
		if (s != null) {
		    sAbsolutePosition += s.relativePosition;
		}
	    }

	    otherTreeMin.setRight(otherTree, null);
	    otherTreeMin.setLeft(s, maxNode);
	    if (otherTree != null) {
		otherTree.min().setLeft(null, otherTreeMin);
		otherTree.relativePosition++;
	    }
	    if (s != null) {
		s.max().setRight(null, otherTreeMin);
		s.relativePosition = sAbsolutePosition - currentSize;
	    }
	    otherTreeMin.relativePosition = currentSize - sParentAbsolutePosition;

	    s = otherTreeMin;
	    while (!sAncestors.isEmpty()) {
		final AVLNode&lt;E&gt; sAncestor = sAncestors.pop();
		sAncestor.setRight(s, null);
		s = sAncestor.balance();
	    }
	    return s;
	}

	/**
	 * Constructs a new AVL tree from a collection.
	 * &lt;p&gt;
	 * This is a recursive helper for {@link #AVLNode(Collection)}. A call
	 * to this method will construct the subtree for elements {@code start}
	 * through {@code end} of the collection, assuming the iterator
	 * {@code e} already points at element {@code start}.
	 *
	 * @param iterator  an iterator over the collection, which should already point
	 *          to the element at index {@code start} within the collection
	 * @param start  the index of the first element in the collection that
	 *          should be in this subtree
	 * @param end  the index of the last element in the collection that
	 *          should be in this subtree
	 * @param absolutePositionOfParent  absolute position of this node's
	 *          parent, or 0 if this node is the root
	 * @param prev  the {@code AVLNode} corresponding to element (start - 1)
	 *          of the collection, or null if start is 0
	 * @param next  the {@code AVLNode} corresponding to element (end + 1)
	 *          of the collection, or null if end is the last element of the collection
	 */
	private AVLNode(final Iterator&lt;? extends E&gt; iterator, final int start, final int end,
		final int absolutePositionOfParent, final AVLNode&lt;E&gt; prev, final AVLNode&lt;E&gt; next) {
	    final int mid = start + (end - start) / 2;
	    if (start &lt; mid) {
		left = new AVLNode&lt;&gt;(iterator, start, mid - 1, mid, prev, this);
	    } else {
		leftIsPrevious = true;
		left = prev;
	    }
	    value = iterator.next();
	    relativePosition = mid - absolutePositionOfParent;
	    if (mid &lt; end) {
		right = new AVLNode&lt;&gt;(iterator, mid + 1, end, mid, this, next);
	    } else {
		rightIsNext = true;
		right = next;
	    }
	    recalcHeight();
	}

	/**
	 * Gets the rightmost child of this node.
	 *
	 * @return the rightmost child (greatest index)
	 */
	private AVLNode&lt;E&gt; max() {
	    return getRightSubTree() == null ? this : right.max();
	}

	/**
	 * Gets the leftmost child of this node.
	 *
	 * @return the leftmost child (smallest index)
	 */
	private AVLNode&lt;E&gt; min() {
	    return getLeftSubTree() == null ? this : left.min();
	}

	private AVLNode&lt;E&gt; removeMax() {
	    if (getRightSubTree() == null) {
		return removeSelf();
	    }
	    setRight(right.removeMax(), right.right);
	    if (relativePosition &lt; 0) {
		relativePosition++;
	    }
	    recalcHeight();
	    return balance();
	}

	/**
	 * Returns the height of the node or -1 if the node is null.
	 */
	private int getHeight(final AVLNode&lt;E&gt; node) {
	    return node == null ? -1 : node.height;
	}

	/**
	 * Sets the left field to the node, or the previous node if that is null
	 *
	 * @param node  the new left subtree node
	 * @param previous  the previous node in the linked list
	 */
	private void setLeft(final AVLNode&lt;E&gt; node, final AVLNode&lt;E&gt; previous) {
	    leftIsPrevious = node == null;
	    left = leftIsPrevious ? previous : node;
	    recalcHeight();
	}

	/**
	 * Sets the right field to the node, or the next node if that is null
	 *
	 * @param node  the new left subtree node
	 * @param next  the next node in the linked list
	 */
	private void setRight(final AVLNode&lt;E&gt; node, final AVLNode&lt;E&gt; next) {
	    rightIsNext = node == null;
	    right = rightIsNext ? next : node;
	    recalcHeight();
	}

	/**
	 * Balances according to the AVL algorithm.
	 */
	private AVLNode&lt;E&gt; balance() {
	    switch (heightRightMinusLeft()) {
	    case 1:
	    case 0:
	    case -1:
		return this;
	    case -2:
		if (left.heightRightMinusLeft() &gt; 0) {
		    setLeft(left.rotateLeft(), null);
		}
		return rotateRight();
	    case 2:
		if (right.heightRightMinusLeft() &lt; 0) {
		    setRight(right.rotateRight(), null);
		}
		return rotateLeft();
	    default:
		throw new RuntimeException("tree inconsistent!");
	    }
	}

	private AVLNode&lt;E&gt; removeMin() {
	    if (getLeftSubTree() == null) {
		return removeSelf();
	    }
	    setLeft(left.removeMin(), left.left);
	    if (relativePosition &gt; 0) {
		relativePosition--;
	    }
	    recalcHeight();
	    return balance();
	}

	/**
	 * Sets the height by calculation.
	 */
	private void recalcHeight() {
	    height = Math.max(getLeftSubTree() == null ? -1 : getLeftSubTree().height,
		    getRightSubTree() == null ? -1 : getRightSubTree().height) + 1;
	}

	/**
	 * Gets the right node, returning null if its a faedelung.
	 */
	private AVLNode&lt;E&gt; getRightSubTree() {
	    return rightIsNext ? null : right;
	}

	/**
	 * Gets the left node, returning null if its a faedelung.
	 */
	private AVLNode&lt;E&gt; getLeftSubTree() {
	    return leftIsPrevious ? null : left;
	}

	/**
	 * Removes this node from the tree.
	 *
	 * @return the node that replaces this one in the parent
	 */
	private AVLNode&lt;E&gt; removeSelf() {
	    if (getRightSubTree() == null && getLeftSubTree() == null) {
		return null;
	    }
	    if (getRightSubTree() == null) {
		if (relativePosition &gt; 0) {
		    left.relativePosition += relativePosition;
		}
		left.max().setRight(null, right);
		return left;
	    }
	    if (getLeftSubTree() == null) {
		right.relativePosition += relativePosition - (relativePosition &lt; 0 ? 0 : 1);
		right.min().setLeft(null, left);
		return right;
	    }

	    if (heightRightMinusLeft() &gt; 0) {
		// more on the right, so delete from the right
		final AVLNode&lt;E&gt; rightMin = right.min();
		value = rightMin.value;
		if (leftIsPrevious) {
		    left = rightMin.left;
		}
		right = right.removeMin();
		if (relativePosition &lt; 0) {
		    relativePosition++;
		}
	    } else {
		// more on the left or equal, so delete from the left
		final AVLNode&lt;E&gt; leftMax = left.max();
		value = leftMax.value;
		if (rightIsNext) {
		    right = leftMax.right;
		}
		final AVLNode&lt;E&gt; leftPrevious = left.left;
		left = left.removeMax();
		if (left == null) {
		    // special case where left that was deleted was a double link
		    // only occurs when height difference is equal
		    left = leftPrevious;
		    leftIsPrevious = true;
		}
		if (relativePosition &gt; 0) {
		    relativePosition--;
		}
	    }
	    recalcHeight();
	    return this;
	}

	/**
	 * Returns the height difference right - left
	 */
	private int heightRightMinusLeft() {
	    return getHeight(getRightSubTree()) - getHeight(getLeftSubTree());
	}

	private AVLNode&lt;E&gt; rotateLeft() {
	    final AVLNode&lt;E&gt; newTop = right; // can't be faedelung!
	    final AVLNode&lt;E&gt; movedNode = getRightSubTree().getLeftSubTree();

	    final int newTopPosition = relativePosition + getOffset(newTop);
	    final int myNewPosition = -newTop.relativePosition;
	    final int movedPosition = getOffset(newTop) + getOffset(movedNode);

	    setRight(movedNode, newTop);
	    newTop.setLeft(this, null);

	    setOffset(newTop, newTopPosition);
	    setOffset(this, myNewPosition);
	    setOffset(movedNode, movedPosition);
	    return newTop;
	}

	private AVLNode&lt;E&gt; rotateRight() {
	    final AVLNode&lt;E&gt; newTop = left; // can't be faedelung
	    final AVLNode&lt;E&gt; movedNode = getLeftSubTree().getRightSubTree();

	    final int newTopPosition = relativePosition + getOffset(newTop);
	    final int myNewPosition = -newTop.relativePosition;
	    final int movedPosition = getOffset(newTop) + getOffset(movedNode);

	    setLeft(movedNode, newTop);
	    newTop.setRight(this, null);

	    setOffset(newTop, newTopPosition);
	    setOffset(this, myNewPosition);
	    setOffset(movedNode, movedPosition);
	    return newTop;
	}

	/**
	 * Gets the relative position.
	 */
	private int getOffset(final AVLNode&lt;E&gt; node) {
	    if (node == null) {
		return 0;
	    }
	    return node.relativePosition;
	}

	/**
	 * Sets the relative position.
	 */
	private int setOffset(final AVLNode&lt;E&gt; node, final int newOffest) {
	    if (node == null) {
		return 0;
	    }
	    final int oldOffset = getOffset(node);
	    node.relativePosition = newOffest;
	    return oldOffset;
	}

    }

}

