class UnicodeSet {
    /**
     * Iteration method that returns the first character in the
     * specified range of this set.
     * @exception ArrayIndexOutOfBoundsException if index is outside
     * the range &lt;code&gt;0..getRangeCount()-1&lt;/code&gt;
     * @see #getRangeCount
     * @see #getRangeEnd
     * @stable ICU 2.0
     */
    public int getRangeStart(int index) {
	return list[index * 2];
    }

    private int[] list;

}

