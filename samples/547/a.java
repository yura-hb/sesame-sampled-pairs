class StrTokenizer implements ListIterator&lt;String&gt;, Cloneable {
    /**
     * Creates a new instance of this Tokenizer. The new instance is reset so
     * that it will be at the start of the token list.
     * If a {@link CloneNotSupportedException} is caught, return &lt;code&gt;null&lt;/code&gt;.
     *
     * @return a new instance of this Tokenizer which has been reset.
     */
    @Override
    public Object clone() {
	try {
	    return cloneReset();
	} catch (final CloneNotSupportedException ex) {
	    return null;
	}
    }

    /** The text to work on. */
    private char chars[];
    /** The current iteration position */
    private int tokenPos;
    /** The parsed tokens */
    private String tokens[];

    /**
     * Creates a new instance of this Tokenizer. The new instance is reset so that
     * it will be at the start of the token list.
     *
     * @return a new instance of this Tokenizer which has been reset.
     * @throws CloneNotSupportedException if there is a problem cloning
     */
    Object cloneReset() throws CloneNotSupportedException {
	// this method exists to enable 100% test coverage
	final StrTokenizer cloned = (StrTokenizer) super.clone();
	if (cloned.chars != null) {
	    cloned.chars = cloned.chars.clone();
	}
	cloned.reset();
	return cloned;
    }

    /**
     * Resets this tokenizer, forgetting all parsing and iteration already completed.
     * &lt;p&gt;
     * This method allows the same tokenizer to be reused for the same String.
     *
     * @return this, to enable chaining
     */
    public StrTokenizer reset() {
	tokenPos = 0;
	tokens = null;
	return this;
    }

}

