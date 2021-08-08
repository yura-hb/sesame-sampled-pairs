class LocalTime implements Temporal, TemporalAdjuster, Comparable&lt;LocalTime&gt;, Serializable {
    /**
     * Compares this time to another time.
     * &lt;p&gt;
     * The comparison is based on the time-line position of the local times within a day.
     * It is "consistent with equals", as defined by {@link Comparable}.
     *
     * @param other  the other time to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    @Override
    public int compareTo(LocalTime other) {
	int cmp = Integer.compare(hour, other.hour);
	if (cmp == 0) {
	    cmp = Integer.compare(minute, other.minute);
	    if (cmp == 0) {
		cmp = Integer.compare(second, other.second);
		if (cmp == 0) {
		    cmp = Integer.compare(nano, other.nano);
		}
	    }
	}
	return cmp;
    }

    /**
     * The hour.
     */
    private final byte hour;
    /**
     * The minute.
     */
    private final byte minute;
    /**
     * The second.
     */
    private final byte second;
    /**
     * The nanosecond.
     */
    private final int nano;

}

