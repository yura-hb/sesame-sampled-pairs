import java.util.Vector;
import javax.swing.AbstractListModel;

class DefaultListModel&lt;E&gt; extends AbstractListModel&lt;E&gt; {
    /**
     * Removes the first (lowest-indexed) occurrence of the argument
     * from this list.
     *
     * @param   obj   the component to be removed
     * @return  &lt;code&gt;true&lt;/code&gt; if the argument was a component of this
     *          list; &lt;code&gt;false&lt;/code&gt; otherwise
     * @see Vector#removeElement(Object)
     */
    public boolean removeElement(Object obj) {
	int index = indexOf(obj);
	boolean rv = delegate.removeElement(obj);
	if (index &gt;= 0) {
	    fireIntervalRemoved(this, index, index);
	}
	return rv;
    }

    private Vector&lt;E&gt; delegate = new Vector&lt;E&gt;();

    /**
     * Searches for the first occurrence of &lt;code&gt;elem&lt;/code&gt;.
     *
     * @param   elem   an object
     * @return  the index of the first occurrence of the argument in this
     *          list; returns &lt;code&gt;-1&lt;/code&gt; if the object is not found
     * @see Vector#indexOf(Object)
     */
    public int indexOf(Object elem) {
	return delegate.indexOf(elem);
    }

}

