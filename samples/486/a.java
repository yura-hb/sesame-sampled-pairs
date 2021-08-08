import com.puppycrawl.tools.checkstyle.api.DetailAST;

abstract class AbstractExpressionHandler {
    /**
     * Get the first line for a given expression.
     *
     * @param startLine   the line we are starting from
     * @param tree        the expression to find the first line for
     *
     * @return the first line of the expression
     */
    protected static int getFirstLine(int startLine, DetailAST tree) {
	int realStart = startLine;
	final int currLine = tree.getLineNo();
	if (currLine &lt; realStart) {
	    realStart = currLine;
	}

	// check children
	for (DetailAST node = tree.getFirstChild(); node != null; node = node.getNextSibling()) {
	    realStart = getFirstLine(realStart, node);
	}

	return realStart;
    }

}

