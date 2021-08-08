import org.eclipse.jdt.core.IJavaElement;

abstract class JavaElement extends PlatformObject implements IJavaElement {
    /**
     * Returns true if this element is an ancestor of the given element,
     * otherwise false.
     */
    public boolean isAncestorOf(IJavaElement e) {
	IJavaElement parentElement = e.getParent();
	while (parentElement != null && !parentElement.equals(this)) {
	    parentElement = parentElement.getParent();
	}
	return parentElement != null;
    }

}

