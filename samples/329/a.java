class TryStatement extends Statement {
    /**
     * Returns the body of this try statement.
     *
     * @return the try body
     */
    public Block getBody() {
	if (this.body == null) {
	    // lazy init must be thread-safe for readers
	    synchronized (this) {
		if (this.body == null) {
		    preLazyInit();
		    this.body = new Block(this.ast);
		    postLazyInit(this.body, BODY_PROPERTY);
		}
	    }
	}
	return this.body;
    }

    /**
     * The body; lazily initialized; defaults to an empty block.
     */
    private Block body = null;
    /**
     * The "body" structural property of this node type (child type: {@link Block}).
     * @since 3.0
     */
    public static final ChildPropertyDescriptor BODY_PROPERTY = new ChildPropertyDescriptor(TryStatement.class, "body",
	    Block.class, MANDATORY, CYCLE_RISK);

}

