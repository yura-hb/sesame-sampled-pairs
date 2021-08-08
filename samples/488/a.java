class ClassUtils {
    /**
     * &lt;p&gt;Gets the package name of a {@code Class}.&lt;/p&gt;
     *
     * @param cls  the class to get the package name for, may be {@code null}.
     * @return the package name or an empty string
     */
    public static String getPackageName(final Class&lt;?&gt; cls) {
	if (cls == null) {
	    return StringUtils.EMPTY;
	}
	return getPackageName(cls.getName());
    }

    /**
     * The package separator character: &lt;code&gt;'&#x2e;' == {@value}&lt;/code&gt;.
     */
    public static final char PACKAGE_SEPARATOR_CHAR = '.';

    /**
     * &lt;p&gt;Gets the package name from a {@code String}.&lt;/p&gt;
     *
     * &lt;p&gt;The string passed in is assumed to be a class name - it is not checked.&lt;/p&gt;
     * &lt;p&gt;If the class is unpackaged, return an empty string.&lt;/p&gt;
     *
     * @param className  the className to get the package name for, may be {@code null}
     * @return the package name or an empty string
     */
    public static String getPackageName(String className) {
	if (StringUtils.isEmpty(className)) {
	    return StringUtils.EMPTY;
	}

	// Strip array encoding
	while (className.charAt(0) == '[') {
	    className = className.substring(1);
	}
	// Strip Object type encoding
	if (className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';') {
	    className = className.substring(1);
	}

	final int i = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
	if (i == -1) {
	    return StringUtils.EMPTY;
	}
	return className.substring(0, i);
    }

}

