import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

class ClasspathUtil {
    /** 
     * removes a classpath entry from the project 
     */
    public static void removeFromProjectClasspath(IJavaProject jp, IFolder folder, IProgressMonitor progressMonitor)
	    throws JavaModelException {
	IClasspathEntry[] cp = jp.getRawClasspath();
	IPath workspaceRelativePath = folder.getFullPath();
	boolean found = doesClasspathContainEntry(jp, cp, workspaceRelativePath, progressMonitor);

	if (found) {
	    IPath projectRelativePath = folder.getProjectRelativePath().addTrailingSeparator();

	    // remove entries that are for the specified folder, account for 
	    // multiple entries, and clean up any exclusion entries to the 
	    // folder being removed.
	    int j = 0;
	    for (int i = 0; i &lt; cp.length; i++) {
		if (!cp[i].getPath().equals(workspaceRelativePath)) {

		    // see if we added the generated source dir as an exclusion pattern to some other entry
		    IPath[] oldExclusions = cp[i].getExclusionPatterns();
		    int m = 0;
		    for (int k = 0; k &lt; oldExclusions.length; k++) {
			if (!oldExclusions[k].equals(projectRelativePath)) {
			    oldExclusions[m] = oldExclusions[k];
			    m++;
			}
		    }

		    if (oldExclusions.length == m) {
			// no exclusions changed, so we do't need to create a new entry
			cp[j] = cp[i];
		    } else {
			// we've removed some exclusion, so create a new entry
			IPath[] newExclusions = new IPath[m];
			System.arraycopy(oldExclusions, 0, newExclusions, 0, m);
			cp[j] = JavaCore.newSourceEntry(cp[i].getPath(), cp[i].getInclusionPatterns(), newExclusions,
				cp[i].getOutputLocation(), cp[i].getExtraAttributes());
		    }

		    j++;
		}
	    }

	    // now copy updated classpath entries into new array
	    IClasspathEntry[] newCp = new IClasspathEntry[j];
	    System.arraycopy(cp, 0, newCp, 0, j);
	    jp.setRawClasspath(newCp, progressMonitor);

	    if (AptPlugin.DEBUG) {
		AptPlugin.trace("removed " + workspaceRelativePath + " from classpath"); //$NON-NLS-1$ //$NON-NLS-2$
	    }
	}
    }

    /**
     * Does the classpath contain the specified path?
     * @param jp if non-null, get this project's classpath and ignore cp
     * @param cp if non-null, use this classpath and ignore jp
     * @param path the entry to look for on the classpath
     * @param progressMonitor
     * @return true if classpath contains the path specified.
     * @throws JavaModelException
     */
    public static boolean doesClasspathContainEntry(IJavaProject jp, IClasspathEntry[] cp, IPath path,
	    IProgressMonitor progressMonitor) throws JavaModelException {
	if (cp == null)
	    cp = jp.getRawClasspath();
	for (int i = 0; i &lt; cp.length; i++) {
	    if (cp[i].getPath().equals(path)) {
		return true;
	    }
	}
	return false;
    }

}

