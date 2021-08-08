class TreeBidiMap&lt;K, V&gt; implements OrderedBidiMap&lt;K, V&gt;, Serializable {
    /**
     * Checks whether the map is empty or not.
     *
     * @return true if the map is empty
     */
    @Override
    public boolean isEmpty() {
	return nodeCount == 0;
    }

    private transient int nodeCount = 0;

}

