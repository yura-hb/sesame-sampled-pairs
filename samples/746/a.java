import java.util.Vector;
import javax.swing.AbstractListModel;

class DefaultListModel&lt;E&gt; extends AbstractListModel&lt;E&gt; {
    /**
     * Inserts the specified element as a component in this list at the
     * specified &lt;code&gt;index&lt;/code&gt;.
     * &lt;p&gt;
     * Throws an &lt;code&gt;ArrayIndexOutOfBoundsException&lt;/code&gt; if the index
     * is invalid.
     * &lt;blockquote&gt;
     * &lt;b&gt;Note:&lt;/b&gt; Although this method is not deprecated, the preferred
     *    method to use is &lt;code&gt;add(int,Object)&lt;/code&gt;, which implements the
     *    &lt;code&gt;List&lt;/code&gt; interface defined in the 1.2 Collections framework.
     * &lt;/blockquote&gt;
     *
     * @param      element the component to insert
     * @param      index   where to insert the new component
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid
     * @see #add(int,Object)
     * @see Vector#insertElementAt(Object,int)
     */
    public void insertElementAt(E element, int index) {
	delegate.insertElementAt(element, index);
	fireIntervalAdded(this, index, index);
    }

    private Vector&lt;E&gt; delegate = new Vector&lt;E&gt;();

}

