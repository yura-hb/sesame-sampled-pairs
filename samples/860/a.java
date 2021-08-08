import org.eclipse.jdt.internal.core.JavaModelStatus;

class JavaModelException extends CoreException {
    /**
    * Returns the Java model status object for this exception.
    * Equivalent to &lt;code&gt;(IJavaModelStatus) getStatus()&lt;/code&gt;.
    *
    * @return a status object
    */
    public IJavaModelStatus getJavaModelStatus() {
	IStatus status = getStatus();
	if (status instanceof IJavaModelStatus) {
	    return (IJavaModelStatus) status;
	} else {
	    // A regular IStatus is created only in the case of a CoreException.
	    // See bug 13492 Should handle JavaModelExceptions that contains CoreException more gracefully
	    return new JavaModelStatus(this.nestedCoreException);
	}
    }

    CoreException nestedCoreException;

}

