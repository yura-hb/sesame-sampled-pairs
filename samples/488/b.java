import org.eclipse.jdt.core.*;

class PackageFragmentRoot extends Openable implements IPackageFragmentRoot {
    /**
    * Returns the package name for the given folder
    * (which is a decendent of this root).
    */
    protected String getPackageName(IFolder folder) {
	IPath myPath = getPath();
	IPath pkgPath = folder.getFullPath();
	int mySegmentCount = myPath.segmentCount();
	int pkgSegmentCount = pkgPath.segmentCount();
	StringBuffer pkgName = new StringBuffer(IPackageFragment.DEFAULT_PACKAGE_NAME);
	for (int i = mySegmentCount; i &lt; pkgSegmentCount; i++) {
	    if (i &gt; mySegmentCount) {
		pkgName.append('.');
	    }
	    pkgName.append(pkgPath.segment(i));
	}
	return pkgName.toString();
    }

    /**
     * The resource associated with this root (null for external jar)
     */
    protected IResource resource;

    /**
    * @see IJavaElement
    */
    @Override
    public IPath getPath() {
	return internalPath();
    }

    public IPath internalPath() {
	return resource().getFullPath();
    }

    @Override
    public IResource resource() {
	if (this.resource != null) // perf improvement to avoid message send in resource()
	    return this.resource;
	return super.resource();
    }

}

