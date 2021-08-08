import java.util.Vector;
import javax.swing.AbstractListModel;

class DefaultListModel&lt;E&gt; extends AbstractListModel&lt;E&gt; {
    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns (unless it throws an exception).
     */
    public void clear() {
	int index1 = delegate.size() - 1;
	delegate.removeAllElements();
	if (index1 &gt;= 0) {
	    fireIntervalRemoved(this, 0, index1);
	}
    }

    private Vector&lt;E&gt; delegate = new Vector&lt;E&gt;();

}

