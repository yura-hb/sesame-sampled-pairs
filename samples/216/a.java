class TreeBidiMap&lt;K, V&gt; implements OrderedBidiMap&lt;K, V&gt;, Serializable {
    /**
     * Removes all mappings from this map.
     */
    @Override
    public void clear() {
	modify();

	nodeCount = 0;
	rootNode[KEY.ordinal()] = null;
	rootNode[VALUE.ordinal()] = null;
    }

    private transient int nodeCount = 0;
    private transient Node&lt;K, V&gt;[] rootNode;
    private transient int modifications = 0;

    /**
     * increment the modification count -- used to check for
     * concurrent modification of the map through the map and through
     * an Iterator from one of its Set or Collection views
     */
    private void modify() {
	modifications++;
    }

}

