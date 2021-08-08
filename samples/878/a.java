import java.util.Map;

abstract class AbstractMapBag&lt;E&gt; implements Bag&lt;E&gt; {
    /**
     * Removes all copies of the specified object from the bag.
     *
     * @param object the object to remove
     * @return true if the bag changed
     */
    @Override
    public boolean remove(final Object object) {
	final MutableInteger mut = map.get(object);
	if (mut == null) {
	    return false;
	}
	modCount++;
	map.remove(object);
	size -= mut.value;
	return true;
    }

    /** The map to use to store the data */
    private transient Map&lt;E, MutableInteger&gt; map;
    /** The modification count for fail fast iterators */
    private transient int modCount;
    /** The current total size of the bag */
    private int size;

}

