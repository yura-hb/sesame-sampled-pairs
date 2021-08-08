import org.eclipse.jdt.internal.compiler.parser.Scanner;

class TokenScanner {
    /**
     * @return Returns the offset after the current token
     */
    public int getCurrentEndOffset() {
	return this.scanner.getCurrentTokenEndPosition() + 1;
    }

    private final Scanner scanner;

}

