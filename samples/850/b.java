class SDE {
    /**
     * Until the next stratum section, everything after this
     * is in stratumId - so, store the current indicies.
     */
    void storeStratum(String stratumId) {
	/* remove redundant strata */
	if (stratumIndex &gt; 0) {
	    if ((stratumTable[stratumIndex - 1].fileIndex == fileIndex)
		    && (stratumTable[stratumIndex - 1].lineIndex == lineIndex)) {
		/* nothing changed overwrite it */
		--stratumIndex;
	    }
	}
	/* store the results */
	assureStratumTableSize();
	stratumTable[stratumIndex].id = stratumId;
	stratumTable[stratumIndex].fileIndex = fileIndex;
	stratumTable[stratumIndex].lineIndex = lineIndex;
	++stratumIndex;
	currentFileId = 0;
    }

    private int stratumIndex = 0;
    private StratumTableRecord[] stratumTable = null;
    private int fileIndex = 0;
    private int lineIndex = 0;
    private int currentFileId = 0;
    private static final int INIT_SIZE_STRATUM = 3;

    void assureStratumTableSize() {
	int len = stratumTable == null ? 0 : stratumTable.length;
	if (stratumIndex &gt;= len) {
	    int i;
	    int newLen = len == 0 ? INIT_SIZE_STRATUM : len * 2;
	    StratumTableRecord[] newTable = new StratumTableRecord[newLen];
	    for (i = 0; i &lt; len; ++i) {
		newTable[i] = stratumTable[i];
	    }
	    for (; i &lt; newLen; ++i) {
		newTable[i] = new StratumTableRecord();
	    }
	    stratumTable = newTable;
	}
    }

}

