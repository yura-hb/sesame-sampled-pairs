import java.util.Vector;

class DefaultListModel&lt;E&gt; extends AbstractListModel&lt;E&gt; {
    /**
     * Sets the component at the specified {@code index} of this
     * list to be the specified element. The previous component at that
     * position is discarded.
     * &lt;blockquote&gt;
     * &lt;b&gt;Note:&lt;/b&gt; Although this method is not deprecated, the preferred
     *    method to use is {@code set(int,Object)}, which implements the
     *    {@code List} interface defined in the 1.2 Collections framework.
     * &lt;/blockquote&gt;
     *
     * @param      element what the component is to be set to
     * @param      index   the specified index
     * @throws     ArrayIndexOutOfBoundsException if the index is invalid
     * @see #set(int,Object)
     * @see Vector#setElementAt(Object,int)
     */
    public void setElementAt(E element, int index) {
	delegate.setElementAt(element, index);
	fireContentsChanged(this, index, index);
    }

    private Vector&lt;E&gt; delegate = new Vector&lt;E&gt;();

}

