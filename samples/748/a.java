class SuppressionCommentFilter extends AutomaticBean implements TreeWalkerFilter {
    class Tag implements Comparable&lt;Tag&gt; {
	/**
	 * Compares the position of this tag in the file
	 * with the position of another tag.
	 * @param object the tag to compare with this one.
	 * @return a negative number if this tag is before the other tag,
	 *     0 if they are at the same position, and a positive number if this
	 *     tag is after the other tag.
	 */
	@Override
	public int compareTo(Tag object) {
	    final int result;
	    if (line == object.line) {
		result = Integer.compare(column, object.column);
	    } else {
		result = Integer.compare(line, object.line);
	    }
	    return result;
	}

	/** The line number of the tag. */
	private final int line;
	/** The column number of the tag. */
	private final int column;

    }

}

