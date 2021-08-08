class EnumMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements Serializable, Cloneable {
    /**
     * Removes all mappings from this map.
     */
    public void clear() {
	Arrays.fill(vals, null);
	size = 0;
    }

    /**
     * Array representation of this map.  The ith element is the value
     * to which universe[i] is currently mapped, or null if it isn't
     * mapped to anything, or NULL if it's mapped to null.
     */
    private transient Object[] vals;
    /**
     * The number of mappings in this map.
     */
    private transient int size = 0;

}

