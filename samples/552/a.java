import java.util.Vector;
import javax.swing.AbstractListModel;

class DefaultListModel&lt;E&gt; extends AbstractListModel&lt;E&gt; {
    /**
     * Sets the component at the specified &lt;code&gt;index&lt;/code&gt; of this
     * list to be the specified element. The previous component at that
     * position is discarded.
     * &lt;p&gt;
     * Throws an &lt;code&gt;ArrayIndexOutOfBoundsException&lt;/code&gt; if the index
     * is invalid.
     * &lt;blockquote&gt;
     * &lt;b&gt;Note:&lt;/b&gt; Although this method is not deprecated, the preferred
     *    method to use is &lt;code&gt;set(int,Object)&lt;/code&gt;, which implements the
     *    &lt;code&gt;List&lt;/code&gt; interface defined in the 1.2 Collections framework.
     * &lt;/blockquote&gt;
     *
     * @param      element what the component is to be set to
     * @param      index   the specified index
     * @see #set(int,Object)
     * @see Vector#setElementAt(Object,int)
     */
    public void setElementAt(E element, int index) {
	delegate.setElementAt(element, index);
	fireContentsChanged(this, index, index);
    }

    private Vector&lt;E&gt; delegate = new Vector&lt;E&gt;();

}

