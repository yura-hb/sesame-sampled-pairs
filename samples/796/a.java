import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

class BlockCommentPosition {
    /**
     * Node is on type definition.
     * @param blockComment DetailAST
     * @return true if node is before class, interface, enum or annotation.
     */
    public static boolean isOnType(DetailAST blockComment) {
	return isOnClass(blockComment) || isOnInterface(blockComment) || isOnEnum(blockComment)
		|| isOnAnnotationDef(blockComment);
    }

    /**
     * Node is on class definition.
     * @param blockComment DetailAST
     * @return true if node is before class
     */
    public static boolean isOnClass(DetailAST blockComment) {
	return isOnPlainToken(blockComment, TokenTypes.CLASS_DEF, TokenTypes.LITERAL_CLASS)
		|| isOnTokenWithModifiers(blockComment, TokenTypes.CLASS_DEF)
		|| isOnTokenWithAnnotation(blockComment, TokenTypes.CLASS_DEF);
    }

    /**
     * Node is on interface definition.
     * @param blockComment DetailAST
     * @return true if node is before interface
     */
    public static boolean isOnInterface(DetailAST blockComment) {
	return isOnPlainToken(blockComment, TokenTypes.INTERFACE_DEF, TokenTypes.LITERAL_INTERFACE)
		|| isOnTokenWithModifiers(blockComment, TokenTypes.INTERFACE_DEF)
		|| isOnTokenWithAnnotation(blockComment, TokenTypes.INTERFACE_DEF);
    }

    /**
     * Node is on enum definition.
     * @param blockComment DetailAST
     * @return true if node is before enum
     */
    public static boolean isOnEnum(DetailAST blockComment) {
	return isOnPlainToken(blockComment, TokenTypes.ENUM_DEF, TokenTypes.ENUM)
		|| isOnTokenWithModifiers(blockComment, TokenTypes.ENUM_DEF)
		|| isOnTokenWithAnnotation(blockComment, TokenTypes.ENUM_DEF);
    }

    /**
     * Node is on annotation definition.
     * @param blockComment DetailAST
     * @return true if node is before annotation
     */
    public static boolean isOnAnnotationDef(DetailAST blockComment) {
	return isOnPlainToken(blockComment, TokenTypes.ANNOTATION_DEF, TokenTypes.AT)
		|| isOnTokenWithModifiers(blockComment, TokenTypes.ANNOTATION_DEF)
		|| isOnTokenWithAnnotation(blockComment, TokenTypes.ANNOTATION_DEF);
    }

    /**
     * Checks that block comment is on specified token without any modifiers.
     * @param blockComment block comment start DetailAST
     * @param parentTokenType parent token type
     * @param nextTokenType next token type
     * @return true if block comment is on specified token without modifiers
     */
    private static boolean isOnPlainToken(DetailAST blockComment, int parentTokenType, int nextTokenType) {
	return blockComment.getParent() != null && blockComment.getParent().getType() == parentTokenType
		&& getPrevSiblingSkipComments(blockComment).getChildCount() == 0
		&& getNextSiblingSkipComments(blockComment).getType() == nextTokenType;
    }

    /**
     * Checks that block comment is on specified token with modifiers.
     * @param blockComment block comment start DetailAST
     * @param tokenType parent token type
     * @return true if block comment is on specified token with modifiers
     */
    private static boolean isOnTokenWithModifiers(DetailAST blockComment, int tokenType) {
	return blockComment.getParent() != null && blockComment.getParent().getType() == TokenTypes.MODIFIERS
		&& blockComment.getParent().getParent().getType() == tokenType
		&& getPrevSiblingSkipComments(blockComment) == null;
    }

    /**
     * Checks that block comment is on specified token with annotation.
     * @param blockComment block comment start DetailAST
     * @param tokenType parent token type
     * @return true if block comment is on specified token with annotation
     */
    private static boolean isOnTokenWithAnnotation(DetailAST blockComment, int tokenType) {
	return blockComment.getParent() != null && blockComment.getParent().getType() == TokenTypes.ANNOTATION
		&& getPrevSiblingSkipComments(blockComment.getParent()) == null
		&& blockComment.getParent().getParent().getType() == TokenTypes.MODIFIERS
		&& blockComment.getParent().getParent().getParent().getType() == tokenType
		&& getPrevSiblingSkipComments(blockComment) == null;
    }

    /**
     * Get previous sibling node skipping any comments.
     * @param node current node
     * @return previous sibling
     */
    private static DetailAST getPrevSiblingSkipComments(DetailAST node) {
	DetailAST result = node.getPreviousSibling();
	while (result != null && (result.getType() == TokenTypes.SINGLE_LINE_COMMENT
		|| result.getType() == TokenTypes.BLOCK_COMMENT_BEGIN)) {
	    result = result.getPreviousSibling();
	}
	return result;
    }

    /**
     * Get next sibling node skipping any comment nodes.
     * @param node current node
     * @return next sibling
     */
    private static DetailAST getNextSiblingSkipComments(DetailAST node) {
	DetailAST result = node.getNextSibling();
	while (result.getType() == TokenTypes.SINGLE_LINE_COMMENT
		|| result.getType() == TokenTypes.BLOCK_COMMENT_BEGIN) {
	    result = result.getNextSibling();
	}
	return result;
    }

}

