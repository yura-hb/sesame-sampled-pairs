import com.puppycrawl.tools.checkstyle.api.DetailAST;

abstract class AbstractExpressionHandler {
    /**
     * Determines if two expressions are on the same line.
     *
     * @param ast1   the first expression
     * @param ast2   the second expression
     *
     * @return true if they are, false otherwise
     * @noinspection WeakerAccess
     */
    public static boolean areOnSameLine(DetailAST ast1, DetailAST ast2) {
	return ast1.getLineNo() == ast2.getLineNo();
    }

}

