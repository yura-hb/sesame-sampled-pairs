import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

abstract class AbstractExpressionHandler {
    /**
     * Check the indentation level of modifiers.
     */
    protected void checkModifiers() {
	final DetailAST modifiers = mainAst.findFirstToken(TokenTypes.MODIFIERS);
	for (DetailAST modifier = modifiers.getFirstChild(); modifier != null; modifier = modifier.getNextSibling()) {
	    if (isOnStartOfLine(modifier) && !getIndent().isAcceptable(expandedTabsColumnNo(modifier))) {
		logError(modifier, "modifier", expandedTabsColumnNo(modifier));
	    }
	}
    }

    /** The AST which is handled by this handler. */
    private final DetailAST mainAst;
    /** Indentation amount for this handler. */
    private IndentLevel indent;
    /**
     * The instance of {@code IndentationCheck} using this handler.
     */
    private final IndentationCheck indentCheck;
    /** Containing AST handler. */
    private final AbstractExpressionHandler parent;
    /** Name used during output to user. */
    private final String typeName;

    /**
     * Determines if the given expression is at the start of a line.
     *
     * @param ast   the expression to check
     *
     * @return true if it is, false otherwise
     */
    protected final boolean isOnStartOfLine(DetailAST ast) {
	return getLineStart(ast) == expandedTabsColumnNo(ast);
    }

    /**
     * Get the indentation amount for this handler. For performance reasons,
     * this value is cached. The first time this method is called, the
     * indentation amount is computed and stored. On further calls, the stored
     * value is returned.
     *
     * @return the expected indentation amount
     * @noinspection WeakerAccess
     */
    public final IndentLevel getIndent() {
	if (indent == null) {
	    indent = getIndentImpl();
	}
	return indent;
    }

    /**
     * Get the column number for the start of a given expression, expanding
     * tabs out into spaces in the process.
     *
     * @param ast   the expression to find the start of
     *
     * @return the column number for the start of the expression
     */
    protected final int expandedTabsColumnNo(DetailAST ast) {
	final String line = indentCheck.getLine(ast.getLineNo() - 1);

	return CommonUtil.lengthExpandedTabs(line, ast.getColumnNo(), indentCheck.getIndentationTabWidth());
    }

    /**
     * Log an indentation error.
     *
     * @param ast           the expression that caused the error
     * @param subtypeName   the type of the expression
     * @param actualIndent  the actual indent level of the expression
     */
    protected final void logError(DetailAST ast, String subtypeName, int actualIndent) {
	logError(ast, subtypeName, actualIndent, getIndent());
    }

    /**
     * Get the start of the line for the given expression.
     *
     * @param ast   the expression to find the start of the line for
     *
     * @return the start of the line for the given expression
     */
    protected final int getLineStart(DetailAST ast) {
	return getLineStart(ast.getLineNo());
    }

    /**
     * Compute the indentation amount for this handler.
     *
     * @return the expected indentation amount
     */
    protected IndentLevel getIndentImpl() {
	return parent.getSuggestedChildIndent(this);
    }

    /**
     * Log an indentation error.
     *
     * @param ast            the expression that caused the error
     * @param subtypeName    the type of the expression
     * @param actualIndent   the actual indent level of the expression
     * @param expectedIndent the expected indent level of the expression
     */
    protected final void logError(DetailAST ast, String subtypeName, int actualIndent, IndentLevel expectedIndent) {
	final String typeStr;

	if (subtypeName.isEmpty()) {
	    typeStr = "";
	} else {
	    typeStr = " " + subtypeName;
	}
	String messageKey = IndentationCheck.MSG_ERROR;
	if (expectedIndent.isMultiLevel()) {
	    messageKey = IndentationCheck.MSG_ERROR_MULTI;
	}
	indentCheck.indentationLog(ast.getLineNo(), messageKey, typeName + typeStr, actualIndent, expectedIndent);
    }

    /**
     * Get the start of the line for the given line number.
     *
     * @param lineNo   the line number to find the start for
     *
     * @return the start of the line for the given expression
     */
    protected final int getLineStart(int lineNo) {
	return getLineStart(indentCheck.getLine(lineNo - 1));
    }

    /**
     * Indentation level suggested for a child element. Children don't have
     * to respect this, but most do.
     *
     * @param child  child AST (so suggestion level can differ based on child
     *                  type)
     *
     * @return suggested indentation for child
     * @noinspection WeakerAccess
     */
    public IndentLevel getSuggestedChildIndent(AbstractExpressionHandler child) {
	return new IndentLevel(getIndent(), getBasicOffset());
    }

    /**
     * Get the start of the specified line.
     *
     * @param line   the specified line number
     *
     * @return the start of the specified line
     */
    private int getLineStart(String line) {
	int index = 0;
	while (Character.isWhitespace(line.charAt(index))) {
	    index++;
	}
	return CommonUtil.lengthExpandedTabs(line, index, indentCheck.getIndentationTabWidth());
    }

    /**
     * A shortcut for {@code IndentationCheck} property.
     * @return value of basicOffset property of {@code IndentationCheck}
     */
    protected final int getBasicOffset() {
	return indentCheck.getBasicOffset();
    }

}

