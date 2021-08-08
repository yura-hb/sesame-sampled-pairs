import java.util.Collection;
import java.util.HashMap;

class BeanContextSupport extends BeanContextChildSupport
	implements BeanContext, Serializable, PropertyChangeListener, VetoableChangeListener {
    /**
     * Returns an iterator of all children
     * of this {@code BeanContext}.
     * @return an iterator for all the current BCSChild values
     */
    protected Iterator&lt;BCSChild&gt; bcsChildren() {
	synchronized (children) {
	    return children.values().iterator();
	}
    }

    /**
     * all accesses to the {@code protected HashMap children} field
     * shall be synchronized on that object.
     */
    protected transient HashMap&lt;Object, BCSChild&gt; children;

}

