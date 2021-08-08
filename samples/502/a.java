class TreeMultiset&lt;E&gt; extends AbstractSortedMultiset&lt;E&gt; implements Serializable {
    /**
    * Creates a new, empty multiset, sorted according to the elements' natural order. All elements
    * inserted into the multiset must implement the {@code Comparable} interface. Furthermore, all
    * such elements must be &lt;i&gt;mutually comparable&lt;/i&gt;: {@code e1.compareTo(e2)} must not throw a
    * {@code ClassCastException} for any elements {@code e1} and {@code e2} in the multiset. If the
    * user attempts to add an element to the multiset that violates this constraint (for example, the
    * user attempts to add a string element to a set whose elements are integers), the {@code
    * add(Object)} call will throw a {@code ClassCastException}.
    *
    * &lt;p&gt;The type specification is {@code &lt;E extends Comparable&gt;}, instead of the more specific
    * {@code &lt;E extends Comparable&lt;? super E&gt;&gt;}, to support classes defined without generics.
    */
    public static &lt;E extends Comparable&gt; TreeMultiset&lt;E&gt; create() {
	return new TreeMultiset&lt;E&gt;(Ordering.natural());
    }

    private final transient GeneralRange&lt;E&gt; range;
    private final transient AvlNode&lt;E&gt; header;
    private final transient Reference&lt;AvlNode&lt;E&gt;&gt; rootReference;

    TreeMultiset(Comparator&lt;? super E&gt; comparator) {
	super(comparator);
	this.range = GeneralRange.all(comparator);
	this.header = new AvlNode&lt;E&gt;(null, 1);
	successor(header, header);
	this.rootReference = new Reference&lt;&gt;();
    }

    private static &lt;T&gt; void successor(AvlNode&lt;T&gt; a, AvlNode&lt;T&gt; b) {
	a.succ = b;
	b.pred = a;
    }

    class AvlNode&lt;E&gt; {
	private final transient GeneralRange&lt;E&gt; range;
	private final transient AvlNode&lt;E&gt; header;
	private final transient Reference&lt;AvlNode&lt;E&gt;&gt; rootReference;

	AvlNode(@Nullable E elem, int elemCount) {
	    checkArgument(elemCount &gt; 0);
	    this.elem = elem;
	    this.elemCount = elemCount;
	    this.totalCount = elemCount;
	    this.distinctElements = 1;
	    this.height = 1;
	    this.left = null;
	    this.right = null;
	}

    }

}

