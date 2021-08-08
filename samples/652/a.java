import java.util.Collection;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;

class CompositeMap&lt;K, V&gt; extends AbstractIterableMap&lt;K, V&gt; implements Serializable {
    /**
     * Add an additional Map to the composite.
     *
     * @param map  the Map to be added to the composite
     * @throws IllegalArgumentException if there is a key collision and there is no
     *         MapMutator set to handle it.
     */
    @SuppressWarnings("unchecked")
    public synchronized void addComposited(final Map&lt;K, V&gt; map) throws IllegalArgumentException {
	for (int i = composite.length - 1; i &gt;= 0; --i) {
	    final Collection&lt;K&gt; intersect = CollectionUtils.intersection(this.composite[i].keySet(), map.keySet());
	    if (intersect.size() != 0) {
		if (this.mutator == null) {
		    throw new IllegalArgumentException("Key collision adding Map to CompositeMap");
		}
		this.mutator.resolveCollision(this, this.composite[i], map, intersect);
	    }
	}
	final Map&lt;K, V&gt;[] temp = new Map[this.composite.length + 1];
	System.arraycopy(this.composite, 0, temp, 0, this.composite.length);
	temp[temp.length - 1] = map;
	this.composite = temp;
    }

    /** Array of all maps in the composite */
    private Map&lt;K, V&gt;[] composite;
    /** Handle mutation operations */
    private MapMutator&lt;K, V&gt; mutator;

    interface MapMutator&lt;K, V&gt; {
	/** Array of all maps in the composite */
	private Map&lt;K, V&gt;[] composite;
	/** Handle mutation operations */
	private MapMutator&lt;K, V&gt; mutator;

	/**
	 * Called when adding a new Composited Map results in a
	 * key collision.
	 *
	 * @param composite  the CompositeMap with the collision
	 * @param existing  the Map already in the composite which contains the
	 *        offending key
	 * @param added  the Map being added
	 * @param intersect  the intersection of the keysets of the existing and added maps
	 */
	void resolveCollision(CompositeMap&lt;K, V&gt; composite, Map&lt;K, V&gt; existing, Map&lt;K, V&gt; added,
		Collection&lt;K&gt; intersect);

    }

}

