class SuballocatedByteVector {
    /**
    * Append a byte onto the vector.
    *
    * @param value Byte to add to the list
    */
    public void addElement(byte value) {
	if (m_firstFree &lt; m_blocksize)
	    m_map0[m_firstFree++] = value;
	else {
	    int index = m_firstFree / m_blocksize;
	    int offset = m_firstFree % m_blocksize;
	    ++m_firstFree;

	    if (index &gt;= m_map.length) {
		int newsize = index + m_numblocks;
		byte[][] newMap = new byte[newsize][];
		System.arraycopy(m_map, 0, newMap, 0, m_map.length);
		m_map = newMap;
	    }
	    byte[] block = m_map[index];
	    if (null == block)
		block = m_map[index] = new byte[m_blocksize];
	    block[offset] = value;
	}
    }

    /** Number of bytes in array          */
    protected int m_firstFree = 0;
    /** Size of blocks to allocate          */
    protected int m_blocksize;
    /** "Shortcut" handle to m_map[0] */
    protected byte m_map0[];
    /** Array of arrays of bytes          */
    protected byte m_map[][];
    /** Number of blocks to (over)allocate by */
    protected int m_numblocks = 32;

}

