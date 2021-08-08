import org.eclipse.jdt.core.IClasspathEntry;

class ClasspathEntry implements IClasspathEntry {
    /**
     * Returns a &lt;code&gt;String&lt;/code&gt; for the kind of a class path entry.
     */
    static String kindToString(int kind) {

	switch (kind) {
	case IClasspathEntry.CPE_PROJECT:
	    return "src"; // backward compatibility //$NON-NLS-1$
	case IClasspathEntry.CPE_SOURCE:
	    return "src"; //$NON-NLS-1$
	case IClasspathEntry.CPE_LIBRARY:
	    return "lib"; //$NON-NLS-1$
	case IClasspathEntry.CPE_VARIABLE:
	    return "var"; //$NON-NLS-1$
	case IClasspathEntry.CPE_CONTAINER:
	    return "con"; //$NON-NLS-1$
	case ClasspathEntry.K_OUTPUT:
	    return "output"; //$NON-NLS-1$
	default:
	    return "unknown"; //$NON-NLS-1$
	}
    }

    /**
     * A constant indicating an output location.
     */
    public static final int K_OUTPUT = 10;

}

