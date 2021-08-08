import java.util.Vector;

class DefaultListModel&lt;E&gt; extends AbstractListModel&lt;E&gt; {
    /**
     * Sets the size of this list.
     *
     * @param   newSize   the new size of this list
     * @see Vector#setSize(int)
     */
    public void setSize(int newSize) {
	int oldSize = delegate.size();
	delegate.setSize(newSize);
	if (oldSize &gt; newSize) {
	    fireIntervalRemoved(this, newSize, oldSize - 1);
	} else if (oldSize &lt; newSize) {
	    fireIntervalAdded(this, oldSize, newSize - 1);
	}
    }

    private Vector&lt;E&gt; delegate = new Vector&lt;E&gt;();

}

