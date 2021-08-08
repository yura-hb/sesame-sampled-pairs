class DetailAST extends CommonASTWithHiddenTokens {
    /**
     * Gets the last child node.
     * @return the last child node
     */
    public DetailAST getLastChild() {
	DetailAST ast = getFirstChild();
	while (ast != null && ast.getNextSibling() != null) {
	    ast = ast.getNextSibling();
	}
	return ast;
    }

    @Override
    public DetailAST getFirstChild() {
	return (DetailAST) super.getFirstChild();
    }

    @Override
    public DetailAST getNextSibling() {
	return (DetailAST) super.getNextSibling();
    }

}

