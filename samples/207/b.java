class StringToStringTableVector {
    /**
    * Remove the last element.
    */
    public final void removeLastElem() {

	if (m_firstFree &gt; 0) {
	    m_map[m_firstFree] = null;

	    m_firstFree--;
	}
    }

    /** Number of StringToStringTable objects in this array          */
    private int m_firstFree = 0;
    /** Array of StringToStringTable objects          */
    private StringToStringTable m_map[];

}

