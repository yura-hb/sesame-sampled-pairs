class BlockParentHandler extends AbstractExpressionHandler {
    /**
     * Check the indent of the top level token.
     */
    protected void checkTopLevelToken() {
	final DetailAST topLevel = getTopLevelAst();

	if (topLevel != null && !getIndent().isAcceptable(expandedTabsColumnNo(topLevel))
		&& isOnStartOfLine(topLevel)) {
	    logError(topLevel, "", expandedTabsColumnNo(topLevel));
	}
    }

    /**
     * Get the top level expression being managed by this handler.
     *
     * @return the top level expression
     */
    protected DetailAST getTopLevelAst() {
	return getMainAst();
    }

}

