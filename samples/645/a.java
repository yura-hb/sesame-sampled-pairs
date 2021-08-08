import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.core.NameLookup.Answer;
import org.eclipse.jdt.internal.core.util.Util;

class SearchableEnvironment implements IModuleAwareNameEnvironment, IJavaSearchConstants {
    /**
     * Returns the given type in the the given package if it exists,
     * otherwise &lt;code&gt;null&lt;/code&gt;.
     */
    protected NameEnvironmentAnswer find(String typeName, String packageName, IPackageFragmentRoot[] moduleContext) {
	if (packageName == null)
	    packageName = IPackageFragment.DEFAULT_PACKAGE_NAME;
	if (this.owner != null) {
	    String source = this.owner.findSource(typeName, packageName);
	    if (source != null) {
		IJavaElement moduleElement = (moduleContext != null && moduleContext.length &gt; 0) ? moduleContext[0]
			: null;
		ICompilationUnit cu = new BasicCompilationUnit(source.toCharArray(),
			CharOperation.splitOn('.', packageName.toCharArray()), typeName + Util.defaultJavaExtension(),
			moduleElement);
		return new NameEnvironmentAnswer(cu, null);
	    }
	}
	NameLookup.Answer answer = this.nameLookup.findType(typeName, packageName, false/*exact match*/,
		NameLookup.ACCEPT_ALL, this.checkAccessRestrictions, moduleContext);
	if (answer != null) {
	    // construct name env answer
	    if (answer.type instanceof BinaryType) { // BinaryType
		try {
		    char[] moduleName = answer.module != null ? answer.module.getElementName().toCharArray() : null;
		    return new NameEnvironmentAnswer((IBinaryType) ((BinaryType) answer.type).getElementInfo(),
			    answer.restriction, moduleName);
		} catch (JavaModelException npe) {
		    // fall back to using owner
		}
	    } else { //SourceType
		try {
		    // retrieve the requested type
		    SourceTypeElementInfo sourceType = (SourceTypeElementInfo) ((SourceType) answer.type)
			    .getElementInfo();
		    ISourceType topLevelType = sourceType;
		    while (topLevelType.getEnclosingType() != null) {
			topLevelType = topLevelType.getEnclosingType();
		    }
		    // find all siblings (other types declared in same unit, since may be used for name resolution)
		    IType[] types = sourceType.getHandle().getCompilationUnit().getTypes();
		    ISourceType[] sourceTypes = new ISourceType[types.length];

		    // in the resulting collection, ensure the requested type is the first one
		    sourceTypes[0] = sourceType;
		    int length = types.length;
		    for (int i = 0, index = 1; i &lt; length; i++) {
			ISourceType otherType = (ISourceType) ((JavaElement) types[i]).getElementInfo();
			if (!otherType.equals(topLevelType) && index &lt; length) // check that the index is in bounds (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=62861)
			    sourceTypes[index++] = otherType;
		    }
		    char[] moduleName = answer.module != null ? answer.module.getElementName().toCharArray() : null;
		    return new NameEnvironmentAnswer(sourceTypes, answer.restriction,
			    getExternalAnnotationPath(answer.entry), moduleName);
		} catch (JavaModelException jme) {
		    if (jme.isDoesNotExist() && String.valueOf(TypeConstants.PACKAGE_INFO_NAME).equals(typeName)) {
			// in case of package-info.java the type doesn't exist in the model,
			// but the CU may still help in order to fetch package level annotations.
			return new NameEnvironmentAnswer((ICompilationUnit) answer.type.getParent(),
				answer.restriction);
		    }
		    // no usable answer
		}
	    }
	}
	return null;
    }

    protected WorkingCopyOwner owner;
    public NameLookup nameLookup;
    protected boolean checkAccessRestrictions;
    protected JavaProject project;

    private String getExternalAnnotationPath(IClasspathEntry entry) {
	if (entry == null)
	    return null;
	IPath path = ClasspathEntry.getExternalAnnotationPath(entry, this.project.getProject(), true);
	if (path == null)
	    return null;
	return path.toOSString();
    }

}

