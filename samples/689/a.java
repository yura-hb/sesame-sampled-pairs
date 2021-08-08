import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.core.ICompilationUnit;

class CompilationUnitHelper {
    /**
     * Discard a working copy, ie, remove it from memory. Each call to
     * {@link #getWorkingCopy(String typeName, IPackageFragmentRoot root)} 
     * must be balanced with exactly one call to this method.
     */
    public void discardWorkingCopy(ICompilationUnit wc) {
	if (null == wc)
	    return;
	if (AptPlugin.DEBUG_GFM)
	    AptPlugin.trace("discarding working copy: " + wc.getElementName()); //$NON-NLS-1$
	try {
	    wc.discardWorkingCopy();
	} catch (JavaModelException e) {
	    AptPlugin.log(e, "Unable to discard working copy: " + wc.getElementName()); //$NON-NLS-1$
	}
    }

}

