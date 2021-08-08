class Util {
    /**
     * Put all the arguments in one String.
     */
    public static String getProblemArgumentsForMarker(String[] arguments) {
	StringBuffer args = new StringBuffer(10);

	args.append(arguments.length);
	args.append(':');

	for (int j = 0; j &lt; arguments.length; j++) {
	    if (j != 0)
		args.append(ARGUMENTS_DELIMITER);

	    if (arguments[j].length() == 0) {
		args.append(EMPTY_ARGUMENT);
	    } else {
		encodeArgument(arguments[j], args);
	    }
	}

	return args.toString();
    }

    private static final char ARGUMENTS_DELIMITER = '#';
    private static final String EMPTY_ARGUMENT = "   ";

    /**
     * Encode the argument by doubling the '#' if present into the argument value.
     * 
     * &lt;p&gt;This stores the encoded argument into the given buffer.&lt;/p&gt;
     *
     * @param argument the given argument
     * @param buffer the buffer in which the encoded argument is stored
     */
    private static void encodeArgument(String argument, StringBuffer buffer) {
	for (int i = 0, max = argument.length(); i &lt; max; i++) {
	    char charAt = argument.charAt(i);
	    switch (charAt) {
	    case ARGUMENTS_DELIMITER:
		buffer.append(ARGUMENTS_DELIMITER).append(ARGUMENTS_DELIMITER);
		break;
	    default:
		buffer.append(charAt);
	    }
	}
    }

}

