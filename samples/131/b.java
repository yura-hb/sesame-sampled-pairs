class NodeVector implements Serializable, Cloneable {
    /**
    * Set the length to zero, but don't clear the array.
    */
    public void RemoveAllNoClear() {

	if (null == m_map)
	    return;

	m_firstFree = 0;
    }

    /**
    * Array of nodes this points to.
    *  @serial
    */
    private int m_map[];
    /**
    * Number of nodes in this NodeVector.
    *  @serial
    */
    protected int m_firstFree = 0;

}

