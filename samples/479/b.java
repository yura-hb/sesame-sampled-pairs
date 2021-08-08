class GcInfo implements CompositeData, CompositeDataView {
    /**
     * Returns the elapsed time of this GC in milliseconds.
     *
     * @return the elapsed time of this GC in milliseconds.
     */
    public long getDuration() {
	return endTime - startTime;
    }

    private final long endTime;
    private final long startTime;

}

