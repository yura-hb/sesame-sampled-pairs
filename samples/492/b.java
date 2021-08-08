import java.util.*;

class StyleContext implements Serializable, AttributeContext {
    class KeyBuilder {
	/**
	 * Removes the given name from the set.
	 */
	public void removeAttribute(Object key) {
	    int n = keys.size();
	    for (int i = 0; i &lt; n; i++) {
		if (keys.elementAt(i).equals(key)) {
		    keys.removeElementAt(i);
		    data.removeElementAt(i);
		    return;
		}
	    }
	}

	private Vector&lt;Object&gt; keys = new Vector&lt;Object&gt;();
	private Vector&lt;Object&gt; data = new Vector&lt;Object&gt;();

    }

}

