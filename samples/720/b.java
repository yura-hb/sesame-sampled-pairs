class DecimalStyle {
    /**
     * Checks whether the character is a digit, based on the currently set zero character.
     *
     * @param ch  the character to check
     * @return the value, 0 to 9, of the character, or -1 if not a digit
     */
    int convertToDigit(char ch) {
	int val = ch - zeroDigit;
	return (val &gt;= 0 && val &lt;= 9) ? val : -1;
    }

    /**
     * The zero digit.
     */
    private final char zeroDigit;

}

