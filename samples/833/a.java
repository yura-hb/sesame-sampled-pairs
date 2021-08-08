import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

class DetailAST extends CommonASTWithHiddenTokens {
    /**
     * Gets line number.
     * @return the line number
     */
    public int getLineNo() {
	int resultNo = -1;

	if (lineNo == NOT_INITIALIZED) {
	    // an inner AST that has been initialized
	    // with initialize(String text)
	    resultNo = findLineNo(getFirstChild());

	    if (resultNo == -1) {
		resultNo = findLineNo(getNextSibling());
	    }
	}
	if (resultNo == -1) {
	    resultNo = lineNo;
	}
	return resultNo;
    }

    /** The line number. **/
    private int lineNo = NOT_INITIALIZED;
    /** Constant to indicate if not calculated the child count. */
    private static final int NOT_INITIALIZED = Integer.MIN_VALUE;

    @Override
    public DetailAST getFirstChild() {
	return (DetailAST) super.getFirstChild();
    }

    /**
     * Finds line number in the first non-comment node.
     *
     * @param ast DetailAST node.
     * @return Line number if non-comment node exists, -1 otherwise.
     */
    private static int findLineNo(DetailAST ast) {
	int resultNo = -1;
	DetailAST node = ast;
	while (node != null) {
	    // comment node can't be start of any java statement/definition
	    if (TokenUtil.isCommentType(node.getType())) {
		node = node.getNextSibling();
	    } else {
		resultNo = node.getLineNo();
		break;
	    }
	}
	return resultNo;
    }

    @Override
    public DetailAST getNextSibling() {
	return (DetailAST) super.getNextSibling();
    }

}

