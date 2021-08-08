import org.eclipse.jdt.core.IClasspathEntry;

class ClasspathEntry implements IClasspathEntry {
    /**
     * Returns the kind of a &lt;code&gt;PackageFragmentRoot&lt;/code&gt; from its &lt;code&gt;String&lt;/code&gt; form.
     */
    static int kindFromString(String kindStr) {

	if (kindStr.equalsIgnoreCase("prj")) //$NON-NLS-1$
	    return IClasspathEntry.CPE_PROJECT;
	if (kindStr.equalsIgnoreCase("var")) //$NON-NLS-1$
	    return IClasspathEntry.CPE_VARIABLE;
	if (kindStr.equalsIgnoreCase("con")) //$NON-NLS-1$
	    return IClasspathEntry.CPE_CONTAINER;
	if (kindStr.equalsIgnoreCase("src")) //$NON-NLS-1$
	    return IClasspathEntry.CPE_SOURCE;
	if (kindStr.equalsIgnoreCase("lib")) //$NON-NLS-1$
	    return IClasspathEntry.CPE_LIBRARY;
	if (kindStr.equalsIgnoreCase("output")) //$NON-NLS-1$
	    return ClasspathEntry.K_OUTPUT;
	return -1;
    }

    /**
     * A constant indicating an output location.
     */
    public static final int K_OUTPUT = 10;

}

