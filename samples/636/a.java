class FixedOrderComparator&lt;T&gt; implements Comparator&lt;T&gt;, Serializable {
    /**
     * Checks to see whether the comparator is now locked against further changes.
     *
     * @throws UnsupportedOperationException if the comparator is locked
     */
    protected void checkLocked() {
	if (isLocked()) {
	    throw new UnsupportedOperationException("Cannot modify a FixedOrderComparator after a comparison");
	}
    }

    /** Is the comparator locked against further change */
    private boolean isLocked = false;

    /**
     * Returns true if modifications cannot be made to the FixedOrderComparator.
     * FixedOrderComparators cannot be modified once they have performed a comparison.
     *
     * @return true if attempts to change the FixedOrderComparator yield an
     *  UnsupportedOperationException, false if it can be changed.
     */
    public boolean isLocked() {
	return isLocked;
    }

}

