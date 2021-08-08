import com.sun.org.apache.xml.internal.dtm.DTM;

class SAX2DTM2 extends SAX2DTM {
    class TypedPrecedingSiblingIterator extends PrecedingSiblingIterator {
	/**
	* Return the index of the last node in this iterator.
	*/
	public int getLast() {
	    if (_last != -1)
		return _last;

	    setMark();

	    int node = _currentNode;
	    final int nodeType = _nodeType;
	    final int startNodeID = _startNodeID;

	    int last = 0;
	    if (nodeType != DTM.ELEMENT_NODE) {
		while (node != NULL && node != startNodeID) {
		    if (_exptype2(node) == nodeType) {
			last++;
		    }
		    node = _nextsib2(node);
		}
	    } else {
		while (node != NULL && node != startNodeID) {
		    if (_exptype2(node) &gt;= DTM.NTYPES) {
			last++;
		    }
		    node = _nextsib2(node);
		}
	    }

	    gotoMark();

	    return (_last = last);
	}

	/** The extended type ID that was requested. */
	private final int _nodeType;

    }

    protected int m_blocksize;
    /*******************************************************************
    *                End of nested iterators
    *******************************************************************/

    // %OPT% Array references which are used to cache the map0 arrays in
    // SuballocatedIntVectors. Using the cached arrays reduces the level
    // of indirection and results in better performance than just calling
    // SuballocatedIntVector.elementAt().
    private int[] m_exptype_map0;
    private int[][] m_exptype_map;
    protected int m_SHIFT;
    protected int m_MASK;
    private int[] m_nextsib_map0;
    private int[][] m_nextsib_map;

    /**
    * The optimized version of DTMDefaultBase._exptype().
    *
    * @param identity A node identity, which &lt;em&gt;must not&lt;/em&gt; be equal to
    *        &lt;code&gt;DTM.NULL&lt;/code&gt;
    */
    public final int _exptype2(int identity) {
	//return m_exptype.get(identity);

	if (identity &lt; m_blocksize)
	    return m_exptype_map0[identity];
	else
	    return m_exptype_map[identity &gt;&gt;&gt; m_SHIFT][identity & m_MASK];
    }

    /**
    * The optimized version of DTMDefaultBase._nextsib().
    *
    * @param identity A node identity, which &lt;em&gt;must not&lt;/em&gt; be equal to
    *        &lt;code&gt;DTM.NULL&lt;/code&gt;
    */
    public final int _nextsib2(int identity) {
	//return m_nextsib.get(identity);

	if (identity &lt; m_blocksize)
	    return m_nextsib_map0[identity];
	else
	    return m_nextsib_map[identity &gt;&gt;&gt; m_SHIFT][identity & m_MASK];
    }

}

