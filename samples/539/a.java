import org.eclipse.jdt.internal.core.JavaModelManager.PerProjectInfo;

class SetClasspathOperation extends ChangeClasspathOperation {
    /**
     * Sets the classpath of the pre-specified project.
     */
    @Override
    protected void executeOperation() throws JavaModelException {
	checkCanceled();
	try {
	    // set raw classpath and null out resolved info
	    PerProjectInfo perProjectInfo = this.project.getPerProjectInfo();
	    ClasspathChange classpathChange = perProjectInfo.setRawClasspath(this.newRawClasspath,
		    this.referencedEntries, this.newOutputLocation, JavaModelStatus.VERIFIED_OK/*format is ok*/);

	    // if needed, generate delta, update project ref, create markers, ...
	    classpathChanged(classpathChange, true/*refresh if external linked folder already exists*/);

	    // write .classpath file
	    if (this.canChangeResources && perProjectInfo.writeAndCacheClasspath(this.project, this.newRawClasspath,
		    this.newOutputLocation))
		setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
	} finally {
	    done();
	}
    }

    JavaProject project;
    IClasspathEntry[] newRawClasspath;
    IClasspathEntry[] referencedEntries;
    IPath newOutputLocation;

}

