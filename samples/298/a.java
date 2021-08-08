import java.util.ArrayList;
import java.util.HashMap;
import org.eclipse.jdt.core.*;

abstract class JavaModelOperation implements IWorkspaceRunnable, IProgressMonitor {
    /**
     * Convenience method to create a file
     */
    protected void createFile(IContainer folder, String name, InputStream contents, boolean forceFlag)
	    throws JavaModelException {
	IFile file = folder.getFile(new Path(name));
	try {
	    file.create(contents, forceFlag ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
		    getSubProgressMonitor(1));
	    setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
	} catch (CoreException e) {
	    throw new JavaModelException(e);
	}
    }

    public static final String HAS_MODIFIED_RESOURCE_ATTR = "hasModifiedResource";
    public static final String TRUE = JavaModelManager.TRUE;
    /**
     * The progress monitor passed into this operation
     */
    public SubMonitor progressMonitor = SubMonitor.convert(null);
    protected HashMap attributes;
    protected static final ThreadLocal OPERATION_STACKS = new ThreadLocal();

    /**
     * Creates and returns a subprogress monitor if appropriate.
     */
    protected IProgressMonitor getSubProgressMonitor(int workAmount) {
	return this.progressMonitor.split(workAmount);
    }

    protected static void setAttribute(Object key, Object attribute) {
	ArrayList operationStack = getCurrentOperationStack();
	if (operationStack.size() == 0)
	    return;
	JavaModelOperation topLevelOp = (JavaModelOperation) operationStack.get(0);
	if (topLevelOp.attributes == null) {
	    topLevelOp.attributes = new HashMap();
	}
	topLevelOp.attributes.put(key, attribute);
    }

    protected static ArrayList getCurrentOperationStack() {
	ArrayList stack = (ArrayList) OPERATION_STACKS.get();
	if (stack == null) {
	    stack = new ArrayList();
	    OPERATION_STACKS.set(stack);
	}
	return stack;
    }

}

