import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

class BlockParentHandler extends AbstractExpressionHandler {
    /**
     * Get the child element that is not a list of statements.
     *
     * @return the non-list child element
     */
    protected DetailAST getNonListChild() {
	return getMainAst().findFirstToken(TokenTypes.RPAREN).getNextSibling();
    }

}

