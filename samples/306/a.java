import java.util.Map;

abstract class MultiOperation extends JavaModelOperation {
    /**
     * Returns the parent of the element being copied/moved/renamed.
     */
    protected IJavaElement getDestinationParent(IJavaElement child) {
	return (IJavaElement) this.newParents.get(child);
    }

    /**
     * Table specifying the new parent for elements being
     * copied/moved/renamed.
     * Keyed by elements being processed, and
     * values are the corresponding destination parent.
     */
    protected Map newParents;

}

