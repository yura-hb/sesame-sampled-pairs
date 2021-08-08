class XResourceBundle extends ListResourceBundle {
    /**
     * Get the association list.
     *
     * @return The association list.
     */
    public Object[][] getContents() {
	return new Object[][] { { "ui_language", "en" }, { "help_language", "en" }, { "language", "en" },
		{ "alphabet",
			new CharArrayWrapper(new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
				'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' }) },
		{ "tradAlphabet",
			new CharArrayWrapper(new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
				'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' }) },
		//language orientation
		{ "orientation", "LeftToRight" },
		//language numbering
		{ "numbering", "additive" }, };
    }

}

