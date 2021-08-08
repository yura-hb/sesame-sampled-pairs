class UnconditionalFlowInfo extends FlowInfo {
    /**
    * Remove local variables information from this flow info and return this.
    * @return this, deprived from any local variable information
    */
    public UnconditionalFlowInfo discardNonFieldInitializations() {
	int limit = this.maxFieldCount;
	if (limit &lt; BitCacheSize) {
	    long mask = (1L &lt;&lt; limit) - 1;
	    this.definiteInits &= mask;
	    this.potentialInits &= mask;
	    this.nullBit1 &= mask;
	    this.nullBit2 &= mask;
	    this.nullBit3 &= mask;
	    this.nullBit4 &= mask;
	    this.iNBit &= mask;
	    this.iNNBit &= mask;
	}
	// use extra vector
	if (this.extra == null) {
	    return this; // if vector not yet allocated, then not initialized
	}
	int vectorIndex, length = this.extra[0].length;
	if ((vectorIndex = (limit / BitCacheSize) - 1) &gt;= length) {
	    return this; // not enough room yet
	}
	if (vectorIndex &gt;= 0) {
	    // else we only have complete non field array items left
	    long mask = (1L &lt;&lt; (limit % BitCacheSize)) - 1;
	    for (int j = 0; j &lt; extraLength; j++) {
		this.extra[j][vectorIndex] &= mask;
	    }
	}
	for (int i = vectorIndex + 1; i &lt; length; i++) {
	    for (int j = 0; j &lt; extraLength; j++) {
		this.extra[j][i] = 0;
	    }
	}
	return this;
    }

    public int maxFieldCount;
    public static final int BitCacheSize = 64;
    public long definiteInits;
    public long potentialInits;
    public long nullBit1, nullBit2, nullBit3, nullBit4;
    public long nullBit1, nullBit2, nullBit3, nullBit4;
    public long nullBit1, nullBit2, nullBit3, nullBit4;
    public long nullBit1, nullBit2, nullBit3, nullBit4;
    public long iNBit, // can an incoming null value reach the current point?
	    iNNBit;
    public long iNBit, // can an incoming null value reach the current point?
	    iNNBit;
    public long extra[][];
    public static final int extraLength = 8;

}

