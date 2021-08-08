import java.util.Iterator;

class Iterators {
    /**
    * Returns the single element contained in {@code iterator}, or {@code defaultValue} if the
    * iterator is empty.
    *
    * @throws IllegalArgumentException if the iterator contains multiple elements. The state of the
    *     iterator is unspecified.
    */
    @CanIgnoreReturnValue // TODO(kak): Consider removing this?
    public static &lt;T&gt; @Nullable T getOnlyElement(Iterator&lt;? extends T&gt; iterator, @Nullable T defaultValue) {
	return iterator.hasNext() ? getOnlyElement(iterator) : defaultValue;
    }

    /**
    * Returns the single element contained in {@code iterator}.
    *
    * @throws NoSuchElementException if the iterator is empty
    * @throws IllegalArgumentException if the iterator contains multiple elements. The state of the
    *     iterator is unspecified.
    */
    @CanIgnoreReturnValue // TODO(kak): Consider removing this?
    public static &lt;T&gt; T getOnlyElement(Iterator&lt;T&gt; iterator) {
	T first = iterator.next();
	if (!iterator.hasNext()) {
	    return first;
	}

	StringBuilder sb = new StringBuilder().append("expected one element but was: &lt;").append(first);
	for (int i = 0; i &lt; 4 && iterator.hasNext(); i++) {
	    sb.append(", ").append(iterator.next());
	}
	if (iterator.hasNext()) {
	    sb.append(", ...");
	}
	sb.append('&gt;');

	throw new IllegalArgumentException(sb.toString());
    }

}

