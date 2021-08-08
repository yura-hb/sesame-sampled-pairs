import java.util.Vector;

class DefaultListModel&lt;E&gt; extends AbstractListModel&lt;E&gt; {
    /**
     * Returns an array containing all of the elements in this list in the
     * correct order.
     *
     * @return an array containing the elements of the list
     * @see Vector#toArray()
     */
    public Object[] toArray() {
	Object[] rv = new Object[delegate.size()];
	delegate.copyInto(rv);
	return rv;
    }

    private Vector&lt;E&gt; delegate = new Vector&lt;E&gt;();

}

