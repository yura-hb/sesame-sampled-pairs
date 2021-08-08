import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.commons.collections4.Predicate;

class FilterIterator&lt;E&gt; implements Iterator&lt;E&gt; {
    /**
     * Returns the next object that matches the predicate.
     *
     * @return the next object which matches the given predicate
     * @throws NullPointerException if either the iterator or predicate are null
     * @throws NoSuchElementException if there are no more elements that
     *  match the predicate
     */
    @Override
    public E next() {
	if (!nextObjectSet && !setNextObject()) {
	    throw new NoSuchElementException();
	}
	nextObjectSet = false;
	return nextObject;
    }

    /** Whether the next object has been calculated yet */
    private boolean nextObjectSet = false;
    /** The next object in the iteration */
    private E nextObject;
    /** The iterator being used */
    private Iterator&lt;? extends E&gt; iterator;
    /** The predicate being used */
    private Predicate&lt;? super E&gt; predicate;

    /**
     * Set nextObject to the next object. If there are no more
     * objects then return false. Otherwise, return true.
     */
    private boolean setNextObject() {
	while (iterator.hasNext()) {
	    final E object = iterator.next();
	    if (predicate.evaluate(object)) {
		nextObject = object;
		nextObjectSet = true;
		return true;
	    }
	}
	return false;
    }

}

