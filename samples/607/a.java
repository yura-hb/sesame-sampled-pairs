class FrequencySketch&lt;E&gt; {
    /**
    * Returns if the sketch has not yet been initialized, requiring that {@link #ensureCapacity} is
    * called before it begins to track frequencies.
    */
    public boolean isNotInitialized() {
	return (table == null);
    }

    long[] table;

}

