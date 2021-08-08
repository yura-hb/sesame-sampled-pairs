import java.awt.Color;

class CSS implements Serializable {
    /**
     * Convert a color string such as "RED" or "#NNNNNN" or "rgb(r, g, b)"
     * to a Color.
     */
    static Color stringToColor(String str) {
	Color color;

	if (str == null) {
	    return null;
	}
	if (str.length() == 0)
	    color = Color.black;
	else if (str.startsWith("rgb(")) {
	    color = parseRGB(str);
	} else if (str.charAt(0) == '#')
	    color = hexToColor(str);
	else if (str.equalsIgnoreCase("Black"))
	    color = hexToColor("#000000");
	else if (str.equalsIgnoreCase("Silver"))
	    color = hexToColor("#C0C0C0");
	else if (str.equalsIgnoreCase("Gray"))
	    color = hexToColor("#808080");
	else if (str.equalsIgnoreCase("White"))
	    color = hexToColor("#FFFFFF");
	else if (str.equalsIgnoreCase("Maroon"))
	    color = hexToColor("#800000");
	else if (str.equalsIgnoreCase("Red"))
	    color = hexToColor("#FF0000");
	else if (str.equalsIgnoreCase("Purple"))
	    color = hexToColor("#800080");
	else if (str.equalsIgnoreCase("Fuchsia"))
	    color = hexToColor("#FF00FF");
	else if (str.equalsIgnoreCase("Green"))
	    color = hexToColor("#008000");
	else if (str.equalsIgnoreCase("Lime"))
	    color = hexToColor("#00FF00");
	else if (str.equalsIgnoreCase("Olive"))
	    color = hexToColor("#808000");
	else if (str.equalsIgnoreCase("Yellow"))
	    color = hexToColor("#FFFF00");
	else if (str.equalsIgnoreCase("Navy"))
	    color = hexToColor("#000080");
	else if (str.equalsIgnoreCase("Blue"))
	    color = hexToColor("#0000FF");
	else if (str.equalsIgnoreCase("Teal"))
	    color = hexToColor("#008080");
	else if (str.equalsIgnoreCase("Aqua"))
	    color = hexToColor("#00FFFF");
	else if (str.equalsIgnoreCase("Orange"))
	    color = hexToColor("#FF8000");
	else
	    color = hexToColor(str); // sometimes get specified without leading #
	return color;
    }

    /**
     * Parses a String in the format &lt;code&gt;rgb(r, g, b)&lt;/code&gt; where
     * each of the Color components is either an integer, or a floating number
     * with a % after indicating a percentage value of 255. Values are
     * constrained to fit with 0-255. The resulting Color is returned.
     */
    private static Color parseRGB(String string) {
	// Find the next numeric char
	int[] index = new int[1];

	index[0] = 4;
	int red = getColorComponent(string, index);
	int green = getColorComponent(string, index);
	int blue = getColorComponent(string, index);

	return new Color(red, green, blue);
    }

    /**
      * Convert a "#FFFFFF" hex string to a Color.
      * If the color specification is bad, an attempt
      * will be made to fix it up.
      */
    static final Color hexToColor(String value) {
	String digits;
	int n = value.length();
	if (value.startsWith("#")) {
	    digits = value.substring(1, Math.min(value.length(), 7));
	} else {
	    digits = value;
	}
	String hstr = "0x" + digits;
	Color c;
	try {
	    c = Color.decode(hstr);
	} catch (NumberFormatException nfe) {
	    c = null;
	}
	return c;
    }

    /**
     * Returns the next integer value from &lt;code&gt;string&lt;/code&gt; starting
     * at &lt;code&gt;index[0]&lt;/code&gt;. The value can either can an integer, or
     * a percentage (floating number ending with %), in which case it is
     * multiplied by 255.
     */
    private static int getColorComponent(String string, int[] index) {
	int length = string.length();
	char aChar;

	// Skip non-decimal chars
	while (index[0] &lt; length && (aChar = string.charAt(index[0])) != '-' && !Character.isDigit(aChar)
		&& aChar != '.') {
	    index[0]++;
	}

	int start = index[0];

	if (start &lt; length && string.charAt(index[0]) == '-') {
	    index[0]++;
	}
	while (index[0] &lt; length && Character.isDigit(string.charAt(index[0]))) {
	    index[0]++;
	}
	if (index[0] &lt; length && string.charAt(index[0]) == '.') {
	    // Decimal value
	    index[0]++;
	    while (index[0] &lt; length && Character.isDigit(string.charAt(index[0]))) {
		index[0]++;
	    }
	}
	if (start != index[0]) {
	    try {
		float value = Float.parseFloat(string.substring(start, index[0]));

		if (index[0] &lt; length && string.charAt(index[0]) == '%') {
		    index[0]++;
		    value = value * 255f / 100f;
		}
		return Math.min(255, Math.max(0, (int) value));
	    } catch (NumberFormatException nfe) {
		// Treat as 0
	    }
	}
	return 0;
    }

}

