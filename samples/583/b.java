class ParserContext {
    /**
     * Returns the innermost function in the context.
     * @return the innermost function in the context.
     */
    public ParserContextFunctionNode getCurrentFunction() {
	for (int i = sp - 1; i &gt;= 0; i--) {
	    if (stack[i] instanceof ParserContextFunctionNode) {
		return (ParserContextFunctionNode) stack[i];
	    }
	}
	return null;
    }

    private int sp;
    private ParserContextNode[] stack;

}

