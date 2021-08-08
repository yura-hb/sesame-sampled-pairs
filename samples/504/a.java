import java.util.ArrayList;
import java.util.Map;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.util.Util;

class PackageFragmentRoot extends Openable implements IPackageFragmentRoot {
    /**
    * Compute the package fragment children of this package fragment root.
    *
    * @exception JavaModelException  The resource associated with this package fragment root does not exist
    */
    protected boolean computeChildren(OpenableElementInfo info, IResource underlyingResource)
	    throws JavaModelException {
	// Note the children are not opened (so not added to newElements) for a regular package fragment root
	// However they are opened for a Jar package fragment root (see JarPackageFragmentRoot#computeChildren)
	try {
	    // the underlying resource may be a folder or a project (in the case that the project folder
	    // is actually the package fragment root)
	    if (underlyingResource.getType() == IResource.FOLDER || underlyingResource.getType() == IResource.PROJECT) {
		ArrayList vChildren = new ArrayList(5);
		IContainer rootFolder = (IContainer) underlyingResource;
		char[][] inclusionPatterns = fullInclusionPatternChars();
		char[][] exclusionPatterns = fullExclusionPatternChars();
		computeFolderChildren(rootFolder, !Util.isExcluded(rootFolder, inclusionPatterns, exclusionPatterns),
			CharOperation.NO_STRINGS, vChildren, inclusionPatterns, exclusionPatterns);
		//			char[] suffix = getKind() == K_SOURCE ? SuffixConstants.SUFFIX_java : SuffixConstants.SUFFIX_class;
		//			char[] moduleInfoName = CharOperation.concat(TypeConstants.MODULE_INFO_NAME, suffix);
		//			IResource module = rootFolder.findMember(String.valueOf(moduleInfoName), true);
		//			if (module != null && module.exists()) {
		//				vChildren.add(new ClassFile(getPackageFragment(CharOperation.NO_STRINGS), String.valueOf(TypeConstants.MODULE_INFO_NAME)));
		//			}
		if (!vChildren.isEmpty()) {
		    IJavaElement[] children = new IJavaElement[vChildren.size()];
		    vChildren.toArray(children);
		    info.setChildren(children);
		} else {
		    info.setChildren(JavaElement.NO_ELEMENTS);
		}
	    }
	} catch (JavaModelException e) {
	    //problem resolving children; structure remains unknown
	    info.setChildren(new IJavaElement[] {});
	    throw e;
	}
	return true;
    }

    /**
     * The resource associated with this root (null for external jar)
     */
    protected IResource resource;

    public char[][] fullInclusionPatternChars() {
	try {
	    if (isOpen() && getKind() != IPackageFragmentRoot.K_SOURCE)
		return null;
	    ClasspathEntry entry = (ClasspathEntry) getRawClasspathEntry();
	    if (entry == null) {
		return null;
	    } else {
		return entry.fullInclusionPatternChars();
	    }
	} catch (JavaModelException e) {
	    return null;
	}
    }

    public char[][] fullExclusionPatternChars() {
	try {
	    if (isOpen() && getKind() != IPackageFragmentRoot.K_SOURCE)
		return null;
	    ClasspathEntry entry = (ClasspathEntry) getRawClasspathEntry();
	    if (entry == null) {
		return null;
	    } else {
		return entry.fullExclusionPatternChars();
	    }
	} catch (JavaModelException e) {
	    return null;
	}
    }

    /**
    * Starting at this folder, create package fragments and add the fragments that are not excluded
    * to the collection of children.
    *
    * @exception JavaModelException  The resource associated with this package fragment does not exist
    */
    protected void computeFolderChildren(IContainer folder, boolean isIncluded, String[] pkgName, ArrayList vChildren,
	    char[][] inclusionPatterns, char[][] exclusionPatterns) throws JavaModelException {

	if (isIncluded) {
	    IPackageFragment pkg = getPackageFragment(pkgName);
	    vChildren.add(pkg);
	}
	try {
	    IResource[] members = folder.members();
	    boolean hasIncluded = isIncluded;
	    int length = members.length;
	    if (length &gt; 0) {
		// if package fragment root refers to folder in another IProject, then
		// folder.getProject() is different than getJavaProject().getProject()
		// use the other java project's options to verify the name
		IJavaProject otherJavaProject = JavaCore.create(folder.getProject());
		String sourceLevel = otherJavaProject.getOption(JavaCore.COMPILER_SOURCE, true);
		String complianceLevel = otherJavaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		JavaProject javaProject = (JavaProject) getJavaProject();
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		for (int i = 0; i &lt; length; i++) {
		    IResource member = members[i];
		    String memberName = member.getName();

		    switch (member.getType()) {

		    case IResource.FOLDER:
			// recurse into sub folders even even parent not included as a sub folder could be included
			// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=65637)
			if (Util.isValidFolderNameForPackage(memberName, sourceLevel, complianceLevel)) {
			    // eliminate binary output only if nested inside direct subfolders
			    if (javaProject.contains(member)) {
				String[] newNames = Util.arrayConcat(pkgName, manager.intern(memberName));
				boolean isMemberIncluded = !Util.isExcluded(member, inclusionPatterns,
					exclusionPatterns);
				computeFolderChildren((IFolder) member, isMemberIncluded, newNames, vChildren,
					inclusionPatterns, exclusionPatterns);
			    }
			}
			break;
		    case IResource.FILE:
			// inclusion filter may only include files, in which case we still want to include the immediate parent package (lazily)
			if (!hasIncluded && Util.isValidCompilationUnitName(memberName, sourceLevel, complianceLevel)
				&& !Util.isExcluded(member, inclusionPatterns, exclusionPatterns)) {
			    hasIncluded = true;
			    IPackageFragment pkg = getPackageFragment(pkgName);
			    vChildren.add(pkg);
			}
			break;
		    }
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new JavaModelException(e, IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST); // could be thrown by ElementTree when path is not found
	} catch (CoreException e) {
	    throw new JavaModelException(e);
	}
    }

    /**
    * @see IPackageFragmentRoot
    */
    @Override
    public int getKind() throws JavaModelException {
	return ((PackageFragmentRootInfo) getElementInfo()).getRootKind();
    }

    @Override
    public IClasspathEntry getRawClasspathEntry() throws JavaModelException {

	IClasspathEntry rawEntry = null;
	JavaProject project = (JavaProject) getJavaProject();
	project.getResolvedClasspath(); // force the reverse rawEntry cache to be populated
	Map rootPathToRawEntries = project.getPerProjectInfo().rootPathToRawEntries;
	if (rootPathToRawEntries != null) {
	    rawEntry = (IClasspathEntry) rootPathToRawEntries.get(getPath());
	}
	if (rawEntry == null) {
	    throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_NOT_ON_CLASSPATH, this));
	}
	return rawEntry;
    }

    public PackageFragment getPackageFragment(String[] pkgName) {
	return new PackageFragment(this, pkgName);
    }

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

