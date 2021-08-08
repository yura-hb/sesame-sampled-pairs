class MinMaxPriorityQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; {
    /**
    * Removes the element at position {@code index}.
    *
    * &lt;p&gt;Normally this method leaves the elements at up to {@code index - 1}, inclusive, untouched.
    * Under these circumstances, it returns {@code null}.
    *
    * &lt;p&gt;Occasionally, in order to maintain the heap invariant, it must swap a later element of the
    * list with one before {@code index}. Under these circumstances it returns a pair of elements as
    * a {@link MoveDesc}. The first one is the element that was previously at the end of the heap and
    * is now at some position before {@code index}. The second element is the one that was swapped
    * down to replace the element at {@code index}. This fact is used by iterator.remove so as to
    * visit elements during a traversal once and only once.
    */
    @VisibleForTesting
    @CanIgnoreReturnValue
    MoveDesc&lt;E&gt; removeAt(int index) {
	checkPositionIndex(index, size);
	modCount++;
	size--;
	if (size == index) {
	    queue[size] = null;
	    return null;
	}
	E actualLastElement = elementData(size);
	int lastElementAt = heapForIndex(size).swapWithConceptuallyLastElement(actualLastElement);
	if (lastElementAt == index) {
	    // 'actualLastElement' is now at 'lastElementAt', and the element that was at 'lastElementAt'
	    // is now at the end of queue. If that's the element we wanted to remove in the first place,
	    // don't try to (incorrectly) trickle it. Instead, just delete it and we're done.
	    queue[size] = null;
	    return null;
	}
	E toTrickle = elementData(size);
	queue[size] = null;
	MoveDesc&lt;E&gt; changes = fillHole(index, toTrickle);
	if (lastElementAt &lt; index) {
	    // Last element is moved to before index, swapped with trickled element.
	    if (changes == null) {
		// The trickled element is still after index.
		return new MoveDesc&lt;E&gt;(actualLastElement, toTrickle);
	    } else {
		// The trickled element is back before index, but the replaced element
		// has now been moved after index.
		return new MoveDesc&lt;E&gt;(actualLastElement, changes.replaced);
	    }
	}
	// Trickled element was after index to begin with, no adjustment needed.
	return changes;
    }

    private int size;
    private int modCount;
    private Object[] queue;
    private final Heap minHeap;
    private final Heap maxHeap;
    private static final int EVEN_POWERS_OF_TWO = 0x55555555;
    private static final int ODD_POWERS_OF_TWO = 0xaaaaaaaa;

    @SuppressWarnings("unchecked") // we must carefully only allow Es to get in
    E elementData(int index) {
	return (E) queue[index];
    }

    private Heap heapForIndex(int i) {
	return isEvenLevel(i) ? minHeap : maxHeap;
    }

    private MoveDesc&lt;E&gt; fillHole(int index, E toTrickle) {
	Heap heap = heapForIndex(index);
	// We consider elementData(index) a "hole", and we want to fill it
	// with the last element of the heap, toTrickle.
	// Since the last element of the heap is from the bottom level, we
	// optimistically fill index position with elements from lower levels,
	// moving the hole down. In most cases this reduces the number of
	// comparisons with toTrickle, but in some cases we will need to bubble it
	// all the way up again.
	int vacated = heap.fillHoleAt(index);
	// Try to see if toTrickle can be bubbled up min levels.
	int bubbledTo = heap.bubbleUpAlternatingLevels(vacated, toTrickle);
	if (bubbledTo == vacated) {
	    // Could not bubble toTrickle up min levels, try moving
	    // it from min level to max level (or max to min level) and bubble up
	    // there.
	    return heap.tryCrossOverAndBubbleUp(index, vacated, toTrickle);
	} else {
	    return (bubbledTo &lt; index) ? new MoveDesc&lt;E&gt;(toTrickle, elementData(index)) : null;
	}
    }

    @VisibleForTesting
    static boolean isEvenLevel(int index) {
	int oneBased = ~~(index + 1); // for GWT
	checkState(oneBased &gt; 0, "negative index");
	return (oneBased & EVEN_POWERS_OF_TWO) &gt; (oneBased & ODD_POWERS_OF_TWO);
    }

    class Heap {
	private int size;
	private int modCount;
	private Object[] queue;
	private final Heap minHeap;
	private final Heap maxHeap;
	private static final int EVEN_POWERS_OF_TWO = 0x55555555;
	private static final int ODD_POWERS_OF_TWO = 0xaaaaaaaa;

	/**
	* Swap {@code actualLastElement} with the conceptually correct last element of the heap.
	* Returns the index that {@code actualLastElement} now resides in.
	*
	* &lt;p&gt;Since the last element of the array is actually in the middle of the sorted structure, a
	* childless uncle node could be smaller, which would corrupt the invariant if this element
	* becomes the new parent of the uncle. In that case, we first switch the last element with its
	* uncle, before returning.
	*/
	int swapWithConceptuallyLastElement(E actualLastElement) {
	    int parentIndex = getParentIndex(size);
	    if (parentIndex != 0) {
		int grandparentIndex = getParentIndex(parentIndex);
		int uncleIndex = getRightChildIndex(grandparentIndex);
		if (uncleIndex != parentIndex && getLeftChildIndex(uncleIndex) &gt;= size) {
		    E uncleElement = elementData(uncleIndex);
		    if (ordering.compare(uncleElement, actualLastElement) &lt; 0) {
			queue[uncleIndex] = actualLastElement;
			queue[size] = uncleElement;
			return uncleIndex;
		    }
		}
	    }
	    return size;
	}

	private int getParentIndex(int i) {
	    return (i - 1) / 2;
	}

	private int getRightChildIndex(int i) {
	    return i * 2 + 2;
	}

	private int getLeftChildIndex(int i) {
	    return i * 2 + 1;
	}

	/**
	* Fills the hole at {@code index} by moving in the least of its grandchildren to this position,
	* then recursively filling the new hole created.
	*
	* @return the position of the new hole (where the lowest grandchild moved from, that had no
	*     grandchild to replace it)
	*/
	int fillHoleAt(int index) {
	    int minGrandchildIndex;
	    while ((minGrandchildIndex = findMinGrandChild(index)) &gt; 0) {
		queue[index] = elementData(minGrandchildIndex);
		index = minGrandchildIndex;
	    }
	    return index;
	}

	/**
	* Bubbles a value from {@code index} up the levels of this heap, and returns the index the
	* element ended up at.
	*/
	@CanIgnoreReturnValue
	int bubbleUpAlternatingLevels(int index, E x) {
	    while (index &gt; 2) {
		int grandParentIndex = getGrandparentIndex(index);
		E e = elementData(grandParentIndex);
		if (ordering.compare(e, x) &lt;= 0) {
		    break;
		}
		queue[index] = e;
		index = grandParentIndex;
	    }
	    queue[index] = x;
	    return index;
	}

	/**
	* Tries to move {@code toTrickle} from a min to a max level and bubble up there. If it moved
	* before {@code removeIndex} this method returns a pair as described in {@link #removeAt}.
	*/
	MoveDesc&lt;E&gt; tryCrossOverAndBubbleUp(int removeIndex, int vacated, E toTrickle) {
	    int crossOver = crossOver(vacated, toTrickle);
	    if (crossOver == vacated) {
		return null;
	    }
	    // Successfully crossed over from min to max.
	    // Bubble up max levels.
	    E parent;
	    // If toTrickle is moved up to a parent of removeIndex, the parent is
	    // placed in removeIndex position. We must return that to the iterator so
	    // that it knows to skip it.
	    if (crossOver &lt; removeIndex) {
		// We crossed over to the parent level in crossOver, so the parent
		// has already been moved.
		parent = elementData(removeIndex);
	    } else {
		parent = elementData(getParentIndex(removeIndex));
	    }
	    // bubble it up the opposite heap
	    if (otherHeap.bubbleUpAlternatingLevels(crossOver, toTrickle) &lt; removeIndex) {
		return new MoveDesc&lt;E&gt;(toTrickle, parent);
	    } else {
		return null;
	    }
	}

	/** Returns the minimum grand child or -1 if no grand child exists. */
	int findMinGrandChild(int index) {
	    int leftChildIndex = getLeftChildIndex(index);
	    if (leftChildIndex &lt; 0) {
		return -1;
	    }
	    return findMin(getLeftChildIndex(leftChildIndex), 4);
	}

	private int getGrandparentIndex(int i) {
	    return getParentIndex(getParentIndex(i)); // (i - 3) / 4
	}

	/**
	* Crosses an element over to the opposite heap by moving it one level down (or up if there are
	* no elements below it).
	*
	* &lt;p&gt;Returns the new position of the element.
	*/
	int crossOver(int index, E x) {
	    int minChildIndex = findMinChild(index);
	    // TODO(kevinb): split the && into two if's and move crossOverUp so it's
	    // only called when there's no child.
	    if ((minChildIndex &gt; 0) && (ordering.compare(elementData(minChildIndex), x) &lt; 0)) {
		queue[index] = elementData(minChildIndex);
		queue[minChildIndex] = x;
		return minChildIndex;
	    }
	    return crossOverUp(index, x);
	}

	/**
	* Returns the index of minimum value between {@code index} and {@code index + len}, or {@code
	* -1} if {@code index} is greater than {@code size}.
	*/
	int findMin(int index, int len) {
	    if (index &gt;= size) {
		return -1;
	    }
	    checkState(index &gt; 0);
	    int limit = Math.min(index, size - len) + len;
	    int minIndex = index;
	    for (int i = index + 1; i &lt; limit; i++) {
		if (compareElements(i, minIndex) &lt; 0) {
		    minIndex = i;
		}
	    }
	    return minIndex;
	}

	/** Returns the minimum child or {@code -1} if no child exists. */
	int findMinChild(int index) {
	    return findMin(getLeftChildIndex(index), 2);
	}

	/**
	* Moves an element one level up from a min level to a max level (or vice versa). Returns the
	* new position of the element.
	*/
	int crossOverUp(int index, E x) {
	    if (index == 0) {
		queue[0] = x;
		return 0;
	    }
	    int parentIndex = getParentIndex(index);
	    E parentElement = elementData(parentIndex);
	    if (parentIndex != 0) {
		// This is a guard for the case of the childless uncle.
		// Since the end of the array is actually the middle of the heap,
		// a smaller childless uncle can become a child of x when we
		// bubble up alternate levels, violating the invariant.
		int grandparentIndex = getParentIndex(parentIndex);
		int uncleIndex = getRightChildIndex(grandparentIndex);
		if (uncleIndex != parentIndex && getLeftChildIndex(uncleIndex) &gt;= size) {
		    E uncleElement = elementData(uncleIndex);
		    if (ordering.compare(uncleElement, parentElement) &lt; 0) {
			parentIndex = uncleIndex;
			parentElement = uncleElement;
		    }
		}
	    }
	    if (ordering.compare(parentElement, x) &lt; 0) {
		queue[index] = parentElement;
		queue[parentIndex] = x;
		return parentIndex;
	    }
	    queue[index] = x;
	    return index;
	}

	int compareElements(int a, int b) {
	    return ordering.compare(elementData(a), elementData(b));
	}

    }

    class MoveDesc&lt;E&gt; {
	private int size;
	private int modCount;
	private Object[] queue;
	private final Heap minHeap;
	private final Heap maxHeap;
	private static final int EVEN_POWERS_OF_TWO = 0x55555555;
	private static final int ODD_POWERS_OF_TWO = 0xaaaaaaaa;

	MoveDesc(E toTrickle, E replaced) {
	    this.toTrickle = toTrickle;
	    this.replaced = replaced;
	}

    }

}

