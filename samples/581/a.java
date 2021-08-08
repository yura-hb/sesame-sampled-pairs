class MutableDouble extends Number implements Comparable&lt;MutableDouble&gt;, Mutable&lt;Number&gt; {
    /**
     * Compares this mutable to another in ascending order.
     *
     * @param other  the other mutable to compare to, not null
     * @return negative if this is less, zero if equal, positive if greater
     */
    @Override
    public int compareTo(final MutableDouble other) {
	return Double.compare(this.value, other.value);
    }

    /** The mutable value. */
    private double value;

}

