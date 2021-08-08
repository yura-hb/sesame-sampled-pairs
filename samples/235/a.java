import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

class DetailAST extends CommonASTWithHiddenTokens {
    /**
     * Gets column number.
     * @return the column number
     */
    public int getColumnNo() {
	int resultNo = -1;

	if (columnNo == NOT_INITIALIZED) {
	    // an inner AST that has been initialized
	    // with initialize(String text)
	    resultNo = findColumnNo(getFirstChild());

	    if (resultNo == -1) {
		resultNo = findColumnNo(getNextSibling());
	    }
	}
	if (resultNo == -1) {
	    resultNo = columnNo;
	}
	return resultNo;
    }

    /** The column number. **/
    private int columnNo = NOT_INITIALIZED;
    /** Constant to indicate if not calculated the child count. */
    private static final int NOT_INITIALIZED = Integer.MIN_VALUE;

    @Override
    public DetailAST getFirstChild() {
	return (DetailAST) super.getFirstChild();
    }

    /**
     * Finds column number in the first non-comment node.
     *
     * @param ast DetailAST node.
     * @return Column number if non-comment node exists, -1 otherwise.
     */
    private static int findColumnNo(DetailAST ast) {
	int resultNo = -1;
	DetailAST node = ast;
	while (node != null) {
	    // comment node can't be start of any java statement/definition
	    if (TokenUtil.isCommentType(node.getType())) {
		node = node.getNextSibling();
	    } else {
		resultNo = node.getColumnNo();
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

