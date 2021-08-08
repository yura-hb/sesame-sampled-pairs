import com.sun.tools.javac.util.DefinedBy.Api;

class JCDiagnostic implements Diagnostic&lt;JavaFileObject&gt; {
    /**
     * Get the column number within the line of source referred to by this diagnostic.
     * @return  the column number within the line of source referred to by this diagnostic
     */
    @DefinedBy(Api.COMPILER)
    public long getColumnNumber() {
	if (sourcePosition == null) {
	    sourcePosition = new SourcePosition();
	}
	return sourcePosition.getColumnNumber();
    }

    /** source line position (set lazily) */
    private SourcePosition sourcePosition;
    private final DiagnosticPosition position;
    private final DiagnosticSource source;

    class SourcePosition {
	/** source line position (set lazily) */
	private SourcePosition sourcePosition;
	private final DiagnosticPosition position;
	private final DiagnosticSource source;

	SourcePosition() {
	    int n = (position == null ? Position.NOPOS : position.getPreferredPosition());
	    if (n == Position.NOPOS || source == null)
		line = column = -1;
	    else {
		line = source.getLineNumber(n);
		column = source.getColumnNumber(n, true);
	    }
	}

	public int getColumnNumber() {
	    return column;
	}

    }

    interface DiagnosticPosition {
	/** source line position (set lazily) */
	private SourcePosition sourcePosition;
	private final DiagnosticPosition position;
	private final DiagnosticSource source;

	/** Get the position within the file that most accurately defines the
	 *  location for the diagnostic. */
	int getPreferredPosition();

    }

}

