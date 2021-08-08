class Utils {
    /**
     * Converts the filename to classname.
     *
     * @param filename filename to convert
     * @return corresponding classname
     * @throws AssertionError if filename isn't valid filename for class file -
     *                        {@link #isClassFile(String)}
     */
    public static String fileNameToClassName(String filename) {
	assert isClassFile(filename);
	final char nameSeparator = '/';
	return filename.substring(0, filename.length() - CLASSFILE_EXT.length()).replace(nameSeparator, '.');
    }

    public static final String CLASSFILE_EXT = ".class";

    /**
     * Tests if the filename is valid filename for class file.
     *
     * @param filename tested filename
     */
    public static boolean isClassFile(String filename) {
	return endsWithIgnoreCase(filename, CLASSFILE_EXT)
		// skip all module-info.class files
		&& !(filename.substring(filename.lastIndexOf('/') + 1, filename.lastIndexOf('.'))
			.equals("module-info"));
    }

    /**
     * Tests if the string ends with the suffix, ignoring case
     * considerations
     *
     * @param string the tested string
     * @param suffix the suffix
     * @return {@code true} if {@code string} ends with the {@code suffix}
     * @see String#endsWith(String)
     */
    public static boolean endsWithIgnoreCase(String string, String suffix) {
	if (string == null || suffix == null) {
	    return false;
	}
	int length = suffix.length();
	int toffset = string.length() - length;
	if (toffset &lt; 0) {
	    return false;
	}
	return string.regionMatches(true, toffset, suffix, 0, length);
    }

}

