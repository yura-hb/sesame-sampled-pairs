import java.util.Vector;

class DefaultListModel&lt;E&gt; extends AbstractListModel&lt;E&gt; {
    /**
     * Adds the specified component to the end of this list.
     *
     * @param   element   the component to be added
     * @see Vector#addElement(Object)
     */
    public void addElement(E element) {
	int index = delegate.size();
	delegate.addElement(element);
	fireIntervalAdded(this, index, index);
    }

    private Vector&lt;E&gt; delegate = new Vector&lt;E&gt;();

}

