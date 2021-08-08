import com.github.benmanes.caffeine.base.UnsafeAccess;

class SingleConsumerQueue&lt;E&gt; extends HeadAndTailRef&lt;E&gt; implements Queue&lt;E&gt;, Serializable {
    /** Returns the last node in the linked list. */
    @NonNull
    static &lt;E&gt; Node&lt;E&gt; findLast(@NonNull Node&lt;E&gt; node) {
	Node&lt;E&gt; next;
	while ((next = node.getNextRelaxed()) != null) {
	    node = next;
	}
	return node;
    }

    class Node&lt;E&gt; {
	@SuppressWarnings("unchecked")
	@Nullable
	Node&lt;E&gt; getNextRelaxed() {
	    return (Node&lt;E&gt;) UnsafeAccess.UNSAFE.getObject(this, NEXT_OFFSET);
	}

    }

}

