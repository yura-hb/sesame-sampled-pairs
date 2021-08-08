import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

class ASTConverter {
    /**
     * This method is used to retrieve the start position of the Ellipsis
     */
    protected int retrieveEllipsisStartPosition(int start, int end) {
	this.scanner.resetTo(start, end);
	try {
	    int token;
	    while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
		switch (token) {
		case TerminalTokens.TokenNameELLIPSIS:
		    return this.scanner.startPosition - 1;
		}
	    }
	} catch (InvalidInputException e) {
	    // ignore
	}
	return -1;

    }

    Scanner scanner;

}

