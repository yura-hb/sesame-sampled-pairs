class FrequencySketch&lt;E&gt; {
    /**
    * Increments the specified counter by 1 if it is not already at the maximum value (15).
    *
    * @param i the table index (16 counters)
    * @param j the counter to increment
    * @return if incremented
    */
    boolean incrementAt(int i, int j) {
	int offset = j &lt;&lt; 2;
	long mask = (0xfL &lt;&lt; offset);
	if ((table[i] & mask) != mask) {
	    table[i] += (1L &lt;&lt; offset);
	    return true;
	}
	return false;
    }

    long[] table;

}

