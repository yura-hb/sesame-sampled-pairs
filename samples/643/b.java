class StyleSheet extends StyleContext {
    class SelectorMapping implements Serializable {
	/**
	 * Returns the specificity for the child selector
	 * &lt;code&gt;selector&lt;/code&gt;.
	 */
	protected int getChildSpecificity(String selector) {
	    // class (.) 100
	    // id (#)    10000
	    char firstChar = selector.charAt(0);
	    int specificity = getSpecificity();

	    if (firstChar == '.') {
		specificity += 100;
	    } else if (firstChar == '#') {
		specificity += 10000;
	    } else {
		specificity += 1;
		if (selector.indexOf('.') != -1) {
		    specificity += 100;
		}
		if (selector.indexOf('#') != -1) {
		    specificity += 10000;
		}
	    }
	    return specificity;
	}

	/**
	 * The specificity for this selector.
	 */
	private int specificity;

	/**
	 * Returns the specificity this mapping represents.
	 */
	public int getSpecificity() {
	    return specificity;
	}

    }

}

