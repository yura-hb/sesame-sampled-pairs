class ProblemHandler implements ProblemSeverities {
    /**
    * Search the line number corresponding to a specific position
    */
    public static final int searchLineNumber(int[] startLineIndexes, int position) {
	if (startLineIndexes == null)
	    return 1;
	int length = startLineIndexes.length;
	if (length == 0)
	    return 1;
	int g = 0, d = length - 1;
	int m = 0;
	while (g &lt;= d) {
	    m = (g + d) / 2;
	    if (position &lt; startLineIndexes[m]) {
		d = m - 1;
	    } else if (position &gt; startLineIndexes[m]) {
		g = m + 1;
	    } else {
		return m + 1;
	    }
	}
	if (position &lt; startLineIndexes[m]) {
	    return m + 1;
	}
	return m + 2;
    }

}

