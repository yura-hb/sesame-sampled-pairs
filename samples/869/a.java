import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.compiler.apt.model.IElementInfo;

abstract class IdeProcessingEnvImpl extends BaseProcessingEnvImpl {
    /**
     * Get the IFile that contains or represents the specified source element.
     * If the element is a package, get the IFile corresponding to its
     * package-info.java file.  If the element is a top-level type, get the
     * IFile corresponding to its type.  If the element is a nested element
     * of some sort (nested type, method, etc.) then get the IFile corresponding
     * to the containing top-level type.
     * If the element is not a source type at all, then return null.
     * @param elem
     * @return may be null
     */
    public IFile getEnclosingIFile(Element elem) {
	// if this cast fails it could be that a non-Eclipse element got passed in somehow.
	IElementInfo impl = (IElementInfo) elem;
	String name = impl.getFileName();
	if (name == null) {
	    return null;
	}
	// The name will be workspace-relative, e.g., /project/src/packages/File.java.
	IFile file = _javaProject.getProject().getParent().getFile(new Path(name));
	return file;
    }

    private final IJavaProject _javaProject;

}

