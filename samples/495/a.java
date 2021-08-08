import java.util.List;
import java.util.Set;

class ListOrderedSet&lt;E&gt; extends AbstractSerializableSetDecorator&lt;E&gt; {
    /**
     * Removes the element at the specified position from the ordered set.
     * Shifts any subsequent elements to the left.
     *
     * @param index the index of the element to be removed
     * @return the element that has been remove from the ordered set
     * @see List#remove(int)
     */
    public E remove(final int index) {
	final E obj = setOrder.remove(index);
	remove(obj);
	return obj;
    }

    /** Internal list to hold the sequence of objects */
    private final List&lt;E&gt; setOrder;

    @Override
    public boolean remove(final Object object) {
	final boolean result = decorated().remove(object);
	if (result) {
	    setOrder.remove(object);
	}
	return result;
    }

}

