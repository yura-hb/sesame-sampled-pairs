class PriorityQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements Serializable {
    /**
     * Removes a single instance of the specified element from this queue,
     * if it is present.  More formally, removes an element {@code e} such
     * that {@code o.equals(e)}, if this queue contains one or more such
     * elements.  Returns {@code true} if and only if this queue contained
     * the specified element (or equivalently, if this queue changed as a
     * result of the call).
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if this queue changed as a result of the call
     */
    public boolean remove(Object o) {
	int i = indexOf(o);
	if (i == -1)
	    return false;
	else {
	    removeAt(i);
	    return true;
	}
    }

    /**
     * Priority queue represented as a balanced binary heap: the two
     * children of queue[n] are queue[2*n+1] and queue[2*(n+1)].  The
     * priority queue is ordered by comparator, or by the elements'
     * natural ordering, if comparator is null: For each node n in the
     * heap and each descendant d of n, n &lt;= d.  The element with the
     * lowest value is in queue[0], assuming the queue is nonempty.
     */
    transient Object[] queue;
    /**
     * The number of elements in the priority queue.
     */
    int size;
    /**
     * The number of times this priority queue has been
     * &lt;i&gt;structurally modified&lt;/i&gt;.  See AbstractList for gory details.
     */
    transient int modCount;
    /**
     * The comparator, or null if priority queue uses elements'
     * natural ordering.
     */
    private final Comparator&lt;? super E&gt; comparator;

    private int indexOf(Object o) {
	if (o != null) {
	    final Object[] es = queue;
	    for (int i = 0, n = size; i &lt; n; i++)
		if (o.equals(es[i]))
		    return i;
	}
	return -1;
    }

    /**
     * Removes the ith element from queue.
     *
     * Normally this method leaves the elements at up to i-1,
     * inclusive, untouched.  Under these circumstances, it returns
     * null.  Occasionally, in order to maintain the heap invariant,
     * it must swap a later element of the list with one earlier than
     * i.  Under these circumstances, this method returns the element
     * that was previously at the end of the list and is now at some
     * position before i. This fact is used by iterator.remove so as to
     * avoid missing traversing elements.
     */
    E removeAt(int i) {
	// assert i &gt;= 0 && i &lt; size;
	final Object[] es = queue;
	modCount++;
	int s = --size;
	if (s == i) // removed last element
	    es[i] = null;
	else {
	    E moved = (E) es[s];
	    es[s] = null;
	    siftDown(i, moved);
	    if (es[i] == moved) {
		siftUp(i, moved);
		if (es[i] != moved)
		    return moved;
	    }
	}
	return null;
    }

    /**
     * Inserts item x at position k, maintaining heap invariant by
     * demoting x down the tree repeatedly until it is less than or
     * equal to its children or is a leaf.
     *
     * @param k the position to fill
     * @param x the item to insert
     */
    private void siftDown(int k, E x) {
	if (comparator != null)
	    siftDownUsingComparator(k, x, queue, size, comparator);
	else
	    siftDownComparable(k, x, queue, size);
    }

    /**
     * Inserts item x at position k, maintaining heap invariant by
     * promoting x up the tree until it is greater than or equal to
     * its parent, or is the root.
     *
     * To simplify and speed up coercions and comparisons, the
     * Comparable and Comparator versions are separated into different
     * methods that are otherwise identical. (Similarly for siftDown.)
     *
     * @param k the position to fill
     * @param x the item to insert
     */
    private void siftUp(int k, E x) {
	if (comparator != null)
	    siftUpUsingComparator(k, x, queue, comparator);
	else
	    siftUpComparable(k, x, queue);
    }

    private static &lt;T&gt; void siftDownUsingComparator(int k, T x, Object[] es, int n, Comparator&lt;? super T&gt; cmp) {
	// assert n &gt; 0;
	int half = n &gt;&gt;&gt; 1;
	while (k &lt; half) {
	    int child = (k &lt;&lt; 1) + 1;
	    Object c = es[child];
	    int right = child + 1;
	    if (right &lt; n && cmp.compare((T) c, (T) es[right]) &gt; 0)
		c = es[child = right];
	    if (cmp.compare(x, (T) c) &lt;= 0)
		break;
	    es[k] = c;
	    k = child;
	}
	es[k] = x;
    }

    private static &lt;T&gt; void siftDownComparable(int k, T x, Object[] es, int n) {
	// assert n &gt; 0;
	Comparable&lt;? super T&gt; key = (Comparable&lt;? super T&gt;) x;
	int half = n &gt;&gt;&gt; 1; // loop while a non-leaf
	while (k &lt; half) {
	    int child = (k &lt;&lt; 1) + 1; // assume left child is least
	    Object c = es[child];
	    int right = child + 1;
	    if (right &lt; n && ((Comparable&lt;? super T&gt;) c).compareTo((T) es[right]) &gt; 0)
		c = es[child = right];
	    if (key.compareTo((T) c) &lt;= 0)
		break;
	    es[k] = c;
	    k = child;
	}
	es[k] = key;
    }

    private static &lt;T&gt; void siftUpUsingComparator(int k, T x, Object[] es, Comparator&lt;? super T&gt; cmp) {
	while (k &gt; 0) {
	    int parent = (k - 1) &gt;&gt;&gt; 1;
	    Object e = es[parent];
	    if (cmp.compare(x, (T) e) &gt;= 0)
		break;
	    es[k] = e;
	    k = parent;
	}
	es[k] = x;
    }

    private static &lt;T&gt; void siftUpComparable(int k, T x, Object[] es) {
	Comparable&lt;? super T&gt; key = (Comparable&lt;? super T&gt;) x;
	while (k &gt; 0) {
	    int parent = (k - 1) &gt;&gt;&gt; 1;
	    Object e = es[parent];
	    if (key.compareTo((T) e) &gt;= 0)
		break;
	    es[k] = e;
	    k = parent;
	}
	es[k] = key;
    }

}

