import java.util.Vector;
import javax.swing.AbstractListModel;

class DefaultListModel&lt;E&gt; extends AbstractListModel&lt;E&gt; {
    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     * &lt;p&gt;
     * Throws an &lt;code&gt;ArrayIndexOutOfBoundsException&lt;/code&gt;
     * if the index is out of range
     * (&lt;code&gt;index &lt; 0 || index &gt;= size()&lt;/code&gt;).
     *
     * @param index index of element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     */
    public E set(int index, E element) {
	E rv = delegate.elementAt(index);
	delegate.setElementAt(element, index);
	fireContentsChanged(this, index, index);
	return rv;
    }

    private Vector&lt;E&gt; delegate = new Vector&lt;E&gt;();

}

