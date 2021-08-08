class BTree {
    /**
     * Debugging method for checking B-tree invariants
     * @return the empty String if B-tree invariants hold, otherwise
     * a human readable report
     * @throws IndexException
     */
    public String getInvariantsErrorReport() throws IndexException {
	InvariantsChecker checker = new InvariantsChecker();
	accept(checker);
	return checker.isValid() ? "" : checker.getMsg(); //$NON-NLS-1$
    }

    protected final Database db;
    protected final long rootPointer;
    protected final int maxRecords;
    protected final int offsetChildren;

    /**
     * Visit all nodes beginning when the visitor comparator
     * returns &gt;= 0 until the visitor visit returns falls.
     *
     * @param visitor
     */
    public boolean accept(IBTreeVisitor visitor) throws IndexException {
	return accept(this.db.getRecPtr(this.rootPointer), visitor);
    }

    private boolean accept(long node, IBTreeVisitor visitor) throws IndexException {
	// If found is false, we are still in search mode.
	// Once found is true visit everything.
	// Return false when ready to quit.

	if (node == 0) {
	    return true;
	}
	if (visitor instanceof IBTreeVisitor2) {
	    ((IBTreeVisitor2) visitor).preNode(node);
	}

	try {
	    Chunk chunk = this.db.getChunk(node);

	    // Binary search to find first record greater or equal.
	    int lower = 0;
	    int upper = this.maxRecords - 1;
	    while (lower &lt; upper && getRecord(chunk, node, upper - 1) == 0) {
		upper--;
	    }
	    while (lower &lt; upper) {
		int middle = (lower + upper) / 2;
		long checkRec = getRecord(chunk, node, middle);
		if (checkRec == 0) {
		    upper = middle;
		} else {
		    int compare = visitor.compare(checkRec);
		    if (compare &gt;= 0) {
			upper = middle;
		    } else {
			lower = middle + 1;
		    }
		}
	    }

	    // Start with first record greater or equal, reuse comparison results.
	    int i = lower;
	    for (; i &lt; this.maxRecords; ++i) {
		long record = getRecord(chunk, node, i);
		if (record == 0)
		    break;

		int compare = visitor.compare(record);
		if (compare &gt; 0) {
		    // Start point is to the left.
		    return accept(getChild(chunk, node, i), visitor);
		} else if (compare == 0) {
		    if (!accept(getChild(chunk, node, i), visitor))
			return false;
		    if (!visitor.visit(record))
			return false;
		}
	    }
	    return accept(getChild(chunk, node, i), visitor);
	} finally {
	    if (visitor instanceof IBTreeVisitor2) {
		((IBTreeVisitor2) visitor).postNode(node);
	    }
	}
    }

    protected final long getRecord(Chunk chunk, long node, int index) {
	return chunk.getRecPtr(node + index * Database.INT_SIZE);
    }

    protected final long getChild(Chunk chunk, long node, int index) {
	return chunk.getRecPtr(node + this.offsetChildren + index * Database.INT_SIZE);
    }

    class InvariantsChecker implements IBTreeVisitor2 {
	protected final Database db;
	protected final long rootPointer;
	protected final int maxRecords;
	protected final int offsetChildren;

	public InvariantsChecker() {
	}

	public boolean isValid() {
	    return this.valid;
	}

	public String getMsg() {
	    return this.msg;
	}

    }

    interface IBTreeVisitor2 {
	protected final Database db;
	protected final long rootPointer;
	protected final int maxRecords;
	protected final int offsetChildren;

	void preNode(long node) throws IndexException;

	void postNode(long node) throws IndexException;

    }

}

