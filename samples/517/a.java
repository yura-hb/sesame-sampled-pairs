import java.util.HashSet;
import org.eclipse.jdt.core.*;

class ModelUpdater {
    /**
     * Converts an &lt;code&gt;IResourceDelta&lt;/code&gt; and its children into
     * the corresponding &lt;code&gt;IJavaElementDelta&lt;/code&gt;s.
     * Return whether the delta corresponds to a resource on the classpath.
     * If it is not a resource on the classpath, it will be added as a non-java
     * resource by the sender of this method.
     */
    protected void traverseDelta(IJavaElementDelta delta, IPackageFragmentRoot root, IJavaProject project) {

	boolean processChildren = true;

	Openable element = (Openable) delta.getElement();
	switch (element.getElementType()) {
	case IJavaElement.JAVA_PROJECT:
	    project = (IJavaProject) element;
	    break;
	case IJavaElement.PACKAGE_FRAGMENT_ROOT:
	    root = (IPackageFragmentRoot) element;
	    break;
	case IJavaElement.COMPILATION_UNIT:
	    // filter out working copies that are not primary (we don't want to add/remove them to/from the package fragment
	    CompilationUnit cu = (CompilationUnit) element;
	    if (cu.isWorkingCopy() && !cu.isPrimary()) {
		return;
	    }
	    // $FALL-THROUGH$
	case IJavaElement.CLASS_FILE:
	    processChildren = false;
	    break;
	}

	switch (delta.getKind()) {
	case IJavaElementDelta.ADDED:
	    elementAdded(element);
	    break;
	case IJavaElementDelta.REMOVED:
	    elementRemoved(element);
	    break;
	case IJavaElementDelta.CHANGED:
	    if ((delta.getFlags() & IJavaElementDelta.F_CONTENT) != 0) {
		elementChanged(element);
	    }
	    break;
	}
	if (processChildren) {
	    IJavaElementDelta[] children = delta.getAffectedChildren();
	    for (int i = 0; i &lt; children.length; i++) {
		IJavaElementDelta childDelta = children[i];
		traverseDelta(childDelta, root, project);
	    }
	}
    }

    HashSet projectsToUpdate = new HashSet();

    /**
     * Processing for an element that has been added:&lt;ul&gt;
     * &lt;li&gt;If the element is a project, do nothing, and do not process
     * children, as when a project is created it does not yet have any
     * natures - specifically a java nature.
     * &lt;li&gt;If the elemet is not a project, process it as added (see
     * &lt;code&gt;basicElementAdded&lt;/code&gt;.
     * &lt;/ul&gt;
     */
    protected void elementAdded(Openable element) {

	int elementType = element.getElementType();
	if (elementType == IJavaElement.JAVA_PROJECT) {
	    // project add is handled by JavaProject.configure() because
	    // when a project is created, it does not yet have a java nature
	    addToParentInfo(element);
	    this.projectsToUpdate.add(element);
	} else {
	    addToParentInfo(element);

	    // Force the element to be closed as it might have been opened
	    // before the resource modification came in and it might have a new child
	    // For example, in an IWorkspaceRunnable:
	    // 1. create a package fragment p using a java model operation
	    // 2. open package p
	    // 3. add file X.java in folder p
	    // When the resource delta comes in, only the addition of p is notified,
	    // but the package p is already opened, thus its children are not recomputed
	    // and it appears empty.
	    close(element);
	}

	switch (elementType) {
	case IJavaElement.PACKAGE_FRAGMENT_ROOT:
	    // when a root is added, and is on the classpath, the project must be updated
	    this.projectsToUpdate.add(element.getJavaProject());
	    break;
	case IJavaElement.PACKAGE_FRAGMENT:
	    // get rid of package fragment cache
	    JavaProject project = (JavaProject) element.getJavaProject();
	    project.resetCaches();
	    break;
	}
    }

    /**
     * Generic processing for a removed element:&lt;ul&gt;
     * &lt;li&gt;Close the element, removing its structure from the cache
     * &lt;li&gt;Remove the element from its parent's cache of children
     * &lt;li&gt;Add a REMOVED entry in the delta
     * &lt;/ul&gt;
     */
    protected void elementRemoved(Openable element) {

	if (element.isOpen()) {
	    close(element);
	}
	removeFromParentInfo(element);
	int elementType = element.getElementType();

	switch (elementType) {
	case IJavaElement.JAVA_MODEL:
	    JavaModelManager.getIndexManager().reset();
	    break;
	case IJavaElement.JAVA_PROJECT:
	    JavaModelManager manager = JavaModelManager.getJavaModelManager();
	    JavaProject javaProject = (JavaProject) element;
	    manager.removePerProjectInfo(javaProject, true /* remove external jar files indexes and timestamps*/);
	    manager.containerRemove(javaProject);
	    break;
	case IJavaElement.PACKAGE_FRAGMENT_ROOT:
	    this.projectsToUpdate.add(element.getJavaProject());
	    break;
	case IJavaElement.PACKAGE_FRAGMENT:
	    // get rid of package fragment cache
	    JavaProject project = (JavaProject) element.getJavaProject();
	    project.resetCaches();
	    break;
	}
    }

    /**
     * Generic processing for elements with changed contents:&lt;ul&gt;
     * &lt;li&gt;The element is closed such that any subsequent accesses will re-open
     * the element reflecting its new structure.
     * &lt;/ul&gt;
     */
    protected void elementChanged(Openable element) {

	close(element);
    }

    /**
     * Adds the given child handle to its parent's cache of children.
     */
    protected void addToParentInfo(Openable child) {

	Openable parent = (Openable) child.getParent();
	if (parent != null && parent.isOpen()) {
	    try {
		OpenableElementInfo info = (OpenableElementInfo) parent.getElementInfo();
		info.addChild(child);
	    } catch (JavaModelException e) {
		// do nothing - we already checked if open
	    }
	}
    }

    /**
     * Closes the given element, which removes it from the cache of open elements.
     */
    protected static void close(Openable element) {

	try {
	    element.close();
	} catch (JavaModelException e) {
	    // do nothing
	}
    }

    /**
     * Removes the given element from its parents cache of children. If the
     * element does not have a parent, or the parent is not currently open,
     * this has no effect.
     */
    protected void removeFromParentInfo(Openable child) {

	Openable parent = (Openable) child.getParent();
	if (parent != null && parent.isOpen()) {
	    try {
		OpenableElementInfo info = (OpenableElementInfo) parent.getElementInfo();
		info.removeChild(child);
	    } catch (JavaModelException e) {
		// do nothing - we already checked if open
	    }
	}
    }

}

