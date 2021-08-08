import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.gui.MainFrameModel.ParseMode;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;

class ParseTreeTablePresentation {
    /**
     * Whether the node is a leaf.
     * @param node the node to check.
     * @return true if the node is a leaf.
     */
    public boolean isLeaf(Object node) {
	return getChildCount(node) == 0;
    }

    /** Parsing mode. */
    private ParseMode parseMode;

    /**
     * Returns the number of children of parent.
     * @param parent the node to count children for.
     * @return the number of children of the node parent.
     */
    public int getChildCount(Object parent) {
	final int result;

	if (parent instanceof DetailNode) {
	    result = ((DetailNode) parent).getChildren().length;
	} else {
	    if (parseMode == ParseMode.JAVA_WITH_JAVADOC_AND_COMMENTS
		    && ((AST) parent).getType() == TokenTypes.COMMENT_CONTENT
		    && JavadocUtil.isJavadocComment(((DetailAST) parent).getParent())) {
		//getChildCount return 0 on COMMENT_CONTENT,
		//but we need to attach javadoc tree, that is separate tree
		result = 1;
	    } else {
		result = ((DetailAST) parent).getChildCount();
	    }
	}

	return result;
    }

}

