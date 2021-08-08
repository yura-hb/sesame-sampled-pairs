import org.eclipse.jdt.internal.compiler.parser.Scanner;

class TokenScanner {
    /**
     * Sets the scanner offset to the given offset.
     * @param offset The offset to set
     */
    public void setOffset(int offset) {
	this.scanner.resetTo(offset, this.endPosition);
    }

    private final Scanner scanner;
    private final int endPosition;

}

