import java.util.regex.Pattern;

class DebugFilter {
    /**
     * Check whether a given input is matched by this filter, and determine the log level.
     */
    public int matchLevel(String input) {
	if (terms == null) {
	    return DebugContext.BASIC_LEVEL;
	} else {
	    int defaultLevel = 0;
	    int level = -1;
	    for (Term t : terms) {
		if (t.isMatchAny()) {
		    defaultLevel = t.level;
		} else if (t.matches(input)) {
		    level = t.level;
		}
	    }
	    return level == -1 ? defaultLevel : level;
	}
    }

    private final Term[] terms;

    class Term {
	private final Term[] terms;

	public boolean isMatchAny() {
	    return pattern == null;
	}

	/**
	 * Determines if a given input is matched by this filter.
	 */
	public boolean matches(String input) {
	    return pattern == null || pattern.matcher(input).matches();
	}

    }

}

