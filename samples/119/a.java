import java.util.Map;
import com.puppycrawl.tools.checkstyle.JavadocDetailNodeParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.gui.MainFrameModel.ParseMode;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;

class ParseTreeTablePresentation {
    /**
     * Return the index of child in parent.  If either {@code parent}
     * or {@code child} is {@code null}, returns -1.
     * If either {@code parent} or {@code child} don't
     * belong to this tree model, returns -1.
     *
     * @param parent a node in the tree, obtained from this data source.
     * @param child the node we are interested in.
     * @return the index of the child in the parent, or -1 if either
     *     {@code child} or {@code parent} are {@code null}
     *     or don't belong to this tree model.
     */
    public int getIndexOfChild(Object parent, Object child) {
	int index = -1;
	for (int i = 0; i &lt; getChildCount(parent); i++) {
	    if (getChild(parent, i).equals(child)) {
		index = i;
		break;
	    }
	}
	return index;
    }

    /** Parsing mode. */
    private ParseMode parseMode;
    /** Cache to store already parsed Javadoc comments. Used for optimisation purposes. */
    private final Map&lt;DetailAST, DetailNode&gt; blockCommentToJavadocTree = new HashMap&lt;&gt;();

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

    /**
     * Returns the child of parent at index.
     * @param parent the node to get a child from.
     * @param index the index of a child.
     * @return the child of parent at index.
     */
    public Object getChild(Object parent, int index) {
	final Object result;

	if (parent instanceof DetailNode) {
	    result = ((DetailNode) parent).getChildren()[index];
	} else {
	    result = getChildAtDetailAst((DetailAST) parent, index);
	}

	return result;
    }

    /**
     * Gets child of DetailAST node at specified index.
     * @param parent DetailAST node
     * @param index child index
     * @return child DetailsAST or DetailNode if child is Javadoc node
     *         and parseMode is JAVA_WITH_JAVADOC_AND_COMMENTS.
     */
    private Object getChildAtDetailAst(DetailAST parent, int index) {
	final Object result;
	if (parseMode == ParseMode.JAVA_WITH_JAVADOC_AND_COMMENTS && parent.getType() == TokenTypes.COMMENT_CONTENT
		&& JavadocUtil.isJavadocComment(parent.getParent())) {
	    result = getJavadocTree(parent.getParent());
	} else {
	    int currentIndex = 0;
	    DetailAST child = parent.getFirstChild();
	    while (currentIndex &lt; index) {
		child = child.getNextSibling();
		currentIndex++;
	    }
	    result = child;
	}

	return result;
    }

    /**
     * Gets Javadoc (DetailNode) tree of specified block comments.
     * @param blockComment Javadoc comment as a block comment
     * @return DetailNode tree
     */
    private DetailNode getJavadocTree(DetailAST blockComment) {
	DetailNode javadocTree = blockCommentToJavadocTree.get(blockComment);
	if (javadocTree == null) {
	    javadocTree = new JavadocDetailNodeParser().parseJavadocAsDetailNode(blockComment).getTree();
	    blockCommentToJavadocTree.put(blockComment, javadocTree);
	}
	return javadocTree;
    }

}

