class TryNode extends LexicalContextStatement implements JoinPredecessor {
    /**
     * Reset the body of this try block
     * @param lc current lexical context
     * @param body new body
     * @return new TryNode or same if unchanged
     */
    public TryNode setBody(final LexicalContext lc, final Block body) {
	if (this.body == body) {
	    return this;
	}
	return Node.replaceInLexicalContext(lc, this,
		new TryNode(this, body, catchBlocks, finallyBody, conversion, inlinedFinallies, exception));
    }

    /** Try statements. */
    private final Block body;
    /** List of catch clauses. */
    private final List&lt;Block&gt; catchBlocks;
    /** Finally clause. */
    private final Block finallyBody;
    private final LocalVariableConversion conversion;
    /**
     * List of inlined finally blocks. The structure of every inlined finally is:
     * Block(LabelNode(label, Block(finally-statements, (JumpStatement|ReturnNode)?))).
     * That is, the block has a single LabelNode statement with the label and a block containing the
     * statements of the inlined finally block with the jump or return statement appended (if the finally
     * block was not terminal; the original jump/return is simply ignored if the finally block itself
     * terminates). The reason for this somewhat strange arrangement is that we didn't want to create a
     * separate class for the (label, BlockStatement pair) but rather reused the already available LabelNode.
     * However, if we simply used List&lt;LabelNode&gt; without wrapping the label nodes in an additional Block,
     * that would've thrown off visitors relying on BlockLexicalContext -- same reason why we never use
     * Statement as the type of bodies of e.g. IfNode, WhileNode etc. but rather blockify them even when they're
     * single statements.
     */
    private final List&lt;Block&gt; inlinedFinallies;
    /** Exception symbol. */
    private final Symbol exception;

    private TryNode(final TryNode tryNode, final Block body, final List&lt;Block&gt; catchBlocks, final Block finallyBody,
	    final LocalVariableConversion conversion, final List&lt;Block&gt; inlinedFinallies, final Symbol exception) {
	super(tryNode);
	this.body = body;
	this.catchBlocks = catchBlocks;
	this.finallyBody = finallyBody;
	this.conversion = conversion;
	this.inlinedFinallies = inlinedFinallies;
	this.exception = exception;
    }

}

