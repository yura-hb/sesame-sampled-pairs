import com.google.common.base.Preconditions;
import java.util.Iterator;

class Iterators {
    /** Returns an unmodifiable view of {@code iterator}. */
    public static &lt;T&gt; UnmodifiableIterator&lt;T&gt; unmodifiableIterator(final Iterator&lt;? extends T&gt; iterator) {
	checkNotNull(iterator);
	if (iterator instanceof UnmodifiableIterator) {
	    @SuppressWarnings("unchecked") // Since it's unmodifiable, the covariant cast is safe
	    UnmodifiableIterator&lt;T&gt; result = (UnmodifiableIterator&lt;T&gt;) iterator;
	    return result;
	}
	return new UnmodifiableIterator&lt;T&gt;() {
	    @Override
	    public boolean hasNext() {
		return iterator.hasNext();
	    }

	    @Override
	    public T next() {
		return iterator.next();
	    }
	};
    }

}

