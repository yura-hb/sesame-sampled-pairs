import java.util.AbstractMap;
import java.util.Map;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

class CommonUtil {
    /**
     * Create block comment from token.
     * @param token
     *        Token object.
     * @return DetailAST with BLOCK_COMMENT type.
     */
    public static DetailAST createBlockCommentNode(Token token) {
	final DetailAST blockComment = new DetailAST();
	blockComment.initialize(TokenTypes.BLOCK_COMMENT_BEGIN, BLOCK_MULTIPLE_COMMENT_BEGIN);

	// column counting begins from 0
	blockComment.setColumnNo(token.getColumn() - 1);
	blockComment.setLineNo(token.getLine());

	final DetailAST blockCommentContent = new DetailAST();
	blockCommentContent.setType(TokenTypes.COMMENT_CONTENT);

	// column counting begins from 0
	// plus length of '/*'
	blockCommentContent.setColumnNo(token.getColumn() - 1 + 2);
	blockCommentContent.setLineNo(token.getLine());
	blockCommentContent.setText(token.getText());

	final DetailAST blockCommentClose = new DetailAST();
	blockCommentClose.initialize(TokenTypes.BLOCK_COMMENT_END, BLOCK_MULTIPLE_COMMENT_END);

	final Map.Entry&lt;Integer, Integer&gt; linesColumns = countLinesColumns(token.getText(), token.getLine(),
		token.getColumn());
	blockCommentClose.setLineNo(linesColumns.getKey());
	blockCommentClose.setColumnNo(linesColumns.getValue());

	blockComment.addChild(blockCommentContent);
	blockComment.addChild(blockCommentClose);
	return blockComment;
    }

    /** Symbols with which multiple comment starts. */
    private static final String BLOCK_MULTIPLE_COMMENT_BEGIN = "/*";
    /** Symbols with which multiple comment ends. */
    private static final String BLOCK_MULTIPLE_COMMENT_END = "*/";

    /**
     * Count lines and columns (in last line) in text.
     * @param text
     *        String.
     * @param initialLinesCnt
     *        initial value of lines counter.
     * @param initialColumnsCnt
     *        initial value of columns counter.
     * @return entry(pair), first element is lines counter, second - columns
     *         counter.
     */
    private static Map.Entry&lt;Integer, Integer&gt; countLinesColumns(String text, int initialLinesCnt,
	    int initialColumnsCnt) {
	int lines = initialLinesCnt;
	int columns = initialColumnsCnt;
	boolean foundCr = false;
	for (char c : text.toCharArray()) {
	    if (c == '\n') {
		foundCr = false;
		lines++;
		columns = 0;
	    } else {
		if (foundCr) {
		    foundCr = false;
		    lines++;
		    columns = 0;
		}
		if (c == '\r') {
		    foundCr = true;
		}
		columns++;
	    }
	}
	if (foundCr) {
	    lines++;
	    columns = 0;
	}
	return new AbstractMap.SimpleEntry&lt;&gt;(lines, columns);
    }

}

