import org.eclipse.jdt.internal.compiler.parser.Scanner;

class TokenScanner {
    /**
     * @return Returns the length of the current token
     */
    public int getCurrentLength() {
	return getCurrentEndOffset() - getCurrentStartOffset();
    }

    private final Scanner scanner;

    /**
     * @return Returns the offset after the current token
     */
    public int getCurrentEndOffset() {
	return this.scanner.getCurrentTokenEndPosition() + 1;
    }

    /**
     * @return Returns the start offset of the current token
     */
    public int getCurrentStartOffset() {
	return this.scanner.getCurrentTokenStartPosition();
    }

}

