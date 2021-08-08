import java.util.Iterator;

class Iterators {
    /**
    * Advances {@code iterator} to the end, returning the last element.
    *
    * @return the last element of {@code iterator}
    * @throws NoSuchElementException if the iterator is empty
    */
    public static &lt;T&gt; T getLast(Iterator&lt;T&gt; iterator) {
	while (true) {
	    T current = iterator.next();
	    if (!iterator.hasNext()) {
		return current;
	    }
	}
    }

}

