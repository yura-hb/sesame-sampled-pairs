import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.NoSuchElementException;

class Iterators {
    /**
    * Returns an iterator that cycles indefinitely over the elements of {@code iterable}.
    *
    * &lt;p&gt;The returned iterator supports {@code remove()} if the provided iterator does. After {@code
    * remove()} is called, subsequent cycles omit the removed element, which is no longer in {@code
    * iterable}. The iterator's {@code hasNext()} method returns {@code true} until {@code iterable}
    * is empty.
    *
    * &lt;p&gt;&lt;b&gt;Warning:&lt;/b&gt; Typical uses of the resulting iterator may produce an infinite loop. You
    * should use an explicit {@code break} or be certain that you will eventually remove all the
    * elements.
    */
    public static &lt;T&gt; Iterator&lt;T&gt; cycle(final Iterable&lt;T&gt; iterable) {
	checkNotNull(iterable);
	return new Iterator&lt;T&gt;() {
	    Iterator&lt;T&gt; iterator = emptyModifiableIterator();

	    @Override
	    public boolean hasNext() {
		/*
		 * Don't store a new Iterator until we know the user can't remove() the last returned
		 * element anymore. Otherwise, when we remove from the old iterator, we may be invalidating
		 * the new one. The result is a ConcurrentModificationException or other bad behavior.
		 *
		 * (If we decide that we really, really hate allocating two Iterators per cycle instead of
		 * one, we can optimistically store the new Iterator and then be willing to throw it out if
		 * the user calls remove().)
		 */
		return iterator.hasNext() || iterable.iterator().hasNext();
	    }

	    @Override
	    public T next() {
		if (!iterator.hasNext()) {
		    iterator = iterable.iterator();
		    if (!iterator.hasNext()) {
			throw new NoSuchElementException();
		    }
		}
		return iterator.next();
	    }

	    @Override
	    public void remove() {
		iterator.remove();
	    }
	};
    }

    /**
    * Returns the empty {@code Iterator} that throws {@link IllegalStateException} instead of {@link
    * UnsupportedOperationException} on a call to {@link Iterator#remove()}.
    */
    // Casting to any type is safe since there are no actual elements.
    @SuppressWarnings("unchecked")
    static &lt;T&gt; Iterator&lt;T&gt; emptyModifiableIterator() {
	return (Iterator&lt;T&gt;) EmptyModifiableIterator.INSTANCE;
    }

}

