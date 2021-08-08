class PriorityQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements Serializable {
    /**
     * Inserts the specified element into this priority queue.
     *
     * @return {@code true} (as specified by {@link Queue#offer})
     * @throws ClassCastException if the specified element cannot be
     *         compared with elements currently in this priority queue
     *         according to the priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
	if (e == null)
	    throw new NullPointerException();
	modCount++;
	int i = size;
	if (i &gt;= queue.length)
	    grow(i + 1);
	siftUp(i, e);
	size = i + 1;
	return true;
    }

    /**
     * The number of times this priority queue has been
     * &lt;i&gt;structurally modified&lt;/i&gt;.  See AbstractList for gory details.
     */
    transient int modCount;
    /**
     * The number of elements in the priority queue.
     */
    int size;
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
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    /**
     * The comparator, or null if priority queue uses elements'
     * natural ordering.
     */
    private final Comparator&lt;? super E&gt; comparator;

    /**
     * Increases the capacity of the array.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
	int oldCapacity = queue.length;
	// Double size if small; else grow by 50%
	int newCapacity = oldCapacity + ((oldCapacity &lt; 64) ? (oldCapacity + 2) : (oldCapacity &gt;&gt; 1));
	// overflow-conscious code
	if (newCapacity - MAX_ARRAY_SIZE &gt; 0)
	    newCapacity = hugeCapacity(minCapacity);
	queue = Arrays.copyOf(queue, newCapacity);
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

    private static int hugeCapacity(int minCapacity) {
	if (minCapacity &lt; 0) // overflow
	    throw new OutOfMemoryError();
	return (minCapacity &gt; MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
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

