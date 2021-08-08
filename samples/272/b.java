class MapMaker {
    /**
    * Sets the minimum total size for the internal hash tables. For example, if the initial capacity
    * is {@code 60}, and the concurrency level is {@code 8}, then eight segments are created, each
    * having a hash table of size eight. Providing a large enough estimate at construction time
    * avoids the need for expensive resizing operations later, but setting this value unnecessarily
    * high wastes memory.
    *
    * @throws IllegalArgumentException if {@code initialCapacity} is negative
    * @throws IllegalStateException if an initial capacity was already set
    */
    @CanIgnoreReturnValue
    public MapMaker initialCapacity(int initialCapacity) {
	checkState(this.initialCapacity == UNSET_INT, "initial capacity was already set to %s", this.initialCapacity);
	checkArgument(initialCapacity &gt;= 0);
	this.initialCapacity = initialCapacity;
	return this;
    }

    int initialCapacity = UNSET_INT;
    static final int UNSET_INT = -1;

}

