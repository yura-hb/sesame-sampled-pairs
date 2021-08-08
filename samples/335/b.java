import com.google.common.base.Preconditions;
import java.util.NoSuchElementException;

class Iterators {
    /**
    * Combines four iterators into a single iterator. The returned iterator iterates across the
    * elements in {@code a}, followed by the elements in {@code b}, followed by the elements in
    * {@code c}, followed by the elements in {@code d}. The source iterators are not polled until
    * necessary.
    *
    * &lt;p&gt;The returned iterator supports {@code remove()} when the corresponding input iterator
    * supports it.
    */
    public static &lt;T&gt; Iterator&lt;T&gt; concat(Iterator&lt;? extends T&gt; a, Iterator&lt;? extends T&gt; b, Iterator&lt;? extends T&gt; c,
	    Iterator&lt;? extends T&gt; d) {
	checkNotNull(a);
	checkNotNull(b);
	checkNotNull(c);
	checkNotNull(d);
	return concat(consumingForArray(a, b, c, d));
    }

    /**
    * Returns an Iterator that walks the specified array, nulling out elements behind it. This can
    * avoid memory leaks when an element is no longer necessary.
    *
    * &lt;p&gt;This is mainly just to avoid the intermediate ArrayDeque in ConsumingQueueIterator.
    */
    private static &lt;T&gt; Iterator&lt;T&gt; consumingForArray(final T... elements) {
	return new UnmodifiableIterator&lt;T&gt;() {
	    int index = 0;

	    @Override
	    public boolean hasNext() {
		return index &lt; elements.length;
	    }

	    @Override
	    public T next() {
		if (!hasNext()) {
		    throw new NoSuchElementException();
		}
		T result = elements[index];
		elements[index] = null;
		index++;
		return result;
	    }
	};
    }

    /**
    * Combines multiple iterators into a single iterator. The returned iterator iterates across the
    * elements of each iterator in {@code inputs}. The input iterators are not polled until
    * necessary.
    *
    * &lt;p&gt;The returned iterator supports {@code remove()} when the corresponding input iterator
    * supports it. The methods of the returned iterator may throw {@code NullPointerException} if any
    * of the input iterators is null.
    */
    public static &lt;T&gt; Iterator&lt;T&gt; concat(Iterator&lt;? extends Iterator&lt;? extends T&gt;&gt; inputs) {
	return new ConcatenatedIterator&lt;T&gt;(inputs);
    }

    /**
    * Returns the empty iterator.
    *
    * &lt;p&gt;The {@link Iterable} equivalent of this method is {@link ImmutableSet#of()}.
    */
    static &lt;T&gt; UnmodifiableIterator&lt;T&gt; emptyIterator() {
	return emptyListIterator();
    }

    /**
    * Returns the empty iterator.
    *
    * &lt;p&gt;The {@link Iterable} equivalent of this method is {@link ImmutableSet#of()}.
    */
    // Casting to any type is safe since there are no actual elements.
    @SuppressWarnings("unchecked")
    static &lt;T&gt; UnmodifiableListIterator&lt;T&gt; emptyListIterator() {
	return (UnmodifiableListIterator&lt;T&gt;) ArrayItr.EMPTY;
    }

    class ConcatenatedIterator&lt;T&gt; implements Iterator&lt;T&gt; {
	ConcatenatedIterator(Iterator&lt;? extends Iterator&lt;? extends T&gt;&gt; metaIterator) {
	    iterator = emptyIterator();
	    topMetaIterator = checkNotNull(metaIterator);
	}

    }

}

