import java.util.regex.Pattern;

class DetectorOptions {
    /**
     * The pattern to use when matching.
     * @return the pattern to use when matching.
     */
    public Pattern getPattern() {
	if (pattern == null) {
	    int options = compileFlags;

	    if (ignoreCase) {
		options |= Pattern.CASE_INSENSITIVE;
	    }
	    pattern = Pattern.compile(format, options);
	}
	return pattern;
    }

    /** Pattern created from format. Lazily initialized. */
    private Pattern pattern;
    /**
     * Flags to compile a regular expression with.
     * See {@link Pattern#flags()}.
     */
    private int compileFlags;
    /** Whether to ignore case when matching. */
    private boolean ignoreCase;
    /**
     * Format of the regular expression to check for.
     */
    private String format;

}

