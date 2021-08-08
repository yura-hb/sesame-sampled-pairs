import java.util.Vector;

class DefaultComboBoxModel&lt;E&gt; extends AbstractListModel&lt;E&gt; implements MutableComboBoxModel&lt;E&gt;, Serializable {
    /**
     * Empties the list.
     */
    public void removeAllElements() {
	if (objects.size() &gt; 0) {
	    int firstIndex = 0;
	    int lastIndex = objects.size() - 1;
	    objects.removeAllElements();
	    selectedObject = null;
	    fireIntervalRemoved(this, firstIndex, lastIndex);
	} else {
	    selectedObject = null;
	}
    }

    Vector&lt;E&gt; objects;
    Object selectedObject;

}

