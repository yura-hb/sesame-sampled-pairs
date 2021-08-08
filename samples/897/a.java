import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

abstract class Statement extends ASTNode {
    /**
     * Sets or clears the leading comment string. The comment
     * string must include the starting and ending comment delimiters,
     * and any embedded linebreaks.
     * &lt;p&gt;
     * A leading comment is a comment that appears before the statement.
     * It may be either a traditional comment or an end-of-line comment.
     * Traditional comments must begin with "/&#42;, may contain line breaks,
     * and must end with "&#42;/. End-of-line comments must begin with "//"
     * (as per JLS 3.7), and must not contain line breaks.
     * &lt;/p&gt;
     * &lt;p&gt;
     * Examples:
     * &lt;code&gt;
     * &lt;pre&gt;
     * setLeadingComment("/&#42; traditional comment &#42;/");  // correct
     * setLeadingComment("missing comment delimiters");  // wrong
     * setLeadingComment("/&#42; unterminated traditional comment ");  // wrong
     * setLeadingComment("/&#42; broken\n traditional comment &#42;/");  // correct
     * setLeadingComment("// end-of-line comment\n");  // correct
     * setLeadingComment("// end-of-line comment without line terminator");  // correct
     * setLeadingComment("// broken\n end-of-line comment\n");  // wrong
     * &lt;/pre&gt;
     * &lt;/code&gt;
     * &lt;/p&gt;
     *
     * @param comment the comment string, or &lt;code&gt;null&lt;/code&gt; if none
     * @exception IllegalArgumentException if the comment string is invalid
     * @deprecated This feature was removed in the 2.1 release because it was
     * only a partial, and inadequate, solution to the issue of associating
     * comments with statements.
     */
    public void setLeadingComment(String comment) {
	if (comment != null) {
	    char[] source = comment.toCharArray();
	    Scanner scanner = this.ast.scanner;
	    scanner.resetTo(0, source.length);
	    scanner.setSource(source);
	    try {
		int token;
		boolean onlyOneComment = false;
		while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
		    switch (token) {
		    case TerminalTokens.TokenNameCOMMENT_BLOCK:
		    case TerminalTokens.TokenNameCOMMENT_JAVADOC:
		    case TerminalTokens.TokenNameCOMMENT_LINE:
			if (onlyOneComment) {
			    throw new IllegalArgumentException();
			}
			onlyOneComment = true;
			break;
		    default:
			onlyOneComment = false;
		    }
		}
		if (!onlyOneComment) {
		    throw new IllegalArgumentException();
		}
	    } catch (InvalidInputException e) {
		throw new IllegalArgumentException(e);
	    }
	}
	// we do not consider the obsolete comment as a structureal property
	// but we protect them nevertheless
	checkModifiable();
	this.optionalLeadingComment = comment;
    }

    /**
     * The leading comment, or &lt;code&gt;null&lt;/code&gt; if none.
     * Defaults to none.
     *
     * @deprecated The leading comment feature was removed in 2.1.
     */
    private String optionalLeadingComment = null;

}

