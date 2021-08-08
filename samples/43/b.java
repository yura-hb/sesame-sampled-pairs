import java.util.Vector;

class DefaultListModel&lt;E&gt; extends AbstractListModel&lt;E&gt; {
    /**
     * Removes all components from this list and sets its size to zero.
     * &lt;blockquote&gt;
     * &lt;b&gt;Note:&lt;/b&gt; Although this method is not deprecated, the preferred
     *    method to use is {@code clear}, which implements the
     *    {@code List} interface defined in the 1.2 Collections framework.
     * &lt;/blockquote&gt;
     *
     * @see #clear()
     * @see Vector#removeAllElements()
     */
    public void removeAllElements() {
	int index1 = delegate.size() - 1;
	delegate.removeAllElements();
	if (index1 &gt;= 0) {
	    fireIntervalRemoved(this, 0, index1);
	}
    }

    private Vector&lt;E&gt; delegate = new Vector&lt;E&gt;();

}

