class IntHashTable {
    /**
     * Removes all mappings from this map.
     */
    public void clear() {
	for (int i = objs.length; --i &gt;= 0;) {
	    objs[i] = null;
	}
	num_bindings = 0;
    }

    protected Object[] objs;
    protected int num_bindings;

}

