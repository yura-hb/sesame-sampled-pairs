class Pattern implements Serializable {
    class Node {
	/**
	 * This method implements the classic accept node.
	 */
	boolean match(Matcher matcher, int i, CharSequence seq) {
	    matcher.last = i;
	    matcher.groups[0] = matcher.first;
	    matcher.groups[1] = matcher.last;
	    return true;
	}

    }

}

