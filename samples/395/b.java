class GapContent extends GapVector implements Content, Serializable {
    /**
     * Compares two marks.
     *
     * @param o1 the first object
     * @param o2 the second object
     * @return &lt; 0 if o1 &lt; o2, 0 if the same, &gt; 0 if o1 &gt; o2
     */
    final int compare(MarkData o1, MarkData o2) {
	if (o1.index &lt; o2.index) {
	    return -1;
	} else if (o1.index &gt; o2.index) {
	    return 1;
	} else {
	    return 0;
	}
    }

}

