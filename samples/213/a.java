import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.core.*;

abstract class JavaModelOperation implements IWorkspaceRunnable, IProgressMonitor {
    /**
     * Main entry point for Java Model operations. Runs a Java Model Operation as an IWorkspaceRunnable
     * if not read-only.
     */
    public void runOperation(IProgressMonitor monitor) throws JavaModelException {
	IJavaModelStatus status = verify();
	if (!status.isOK()) {
	    throw new JavaModelException(status);
	}
	try {
	    if (isReadOnly()) {
		run(monitor);
	    } else {
		// Use IWorkspace.run(...) to ensure that resource changes are batched
		// Note that if the tree is locked, this will throw a CoreException, but this is ok
		// as this operation is modifying the tree (not read-only) and a CoreException will be thrown anyway.
		ResourcesPlugin.getWorkspace().run(this, getSchedulingRule(), IWorkspace.AVOID_UPDATE, monitor);
	    }
	} catch (CoreException ce) {
	    if (ce instanceof JavaModelException) {
		throw (JavaModelException) ce;
	    } else {
		if (ce.getStatus().getCode() == IResourceStatus.OPERATION_FAILED) {
		    Throwable e = ce.getStatus().getException();
		    if (e instanceof JavaModelException) {
			throw (JavaModelException) e;
		    }
		}
		throw new JavaModelException(ce);
	    }
	}
    }

    /**
     * The progress monitor passed into this operation
     */
    public SubMonitor progressMonitor = SubMonitor.convert(null);
    /**
     * The elements created by this operation - empty
     * until the operation actually creates elements.
     */
    protected IJavaElement[] resultElements = NO_ELEMENTS;
    /**
     * The elements this operation operates on,
     * or &lt;code&gt;null&lt;/code&gt; if this operation
     * does not operate on specific elements.
     */
    protected IJavaElement[] elementsToProcess;
    protected int actionsStart = 0;
    protected int actionsEnd = -1;
    protected IPostAction[] actions;
    protected static boolean POST_ACTION_VERBOSE;
    public static final String HAS_MODIFIED_RESOURCE_ATTR = "hasModifiedResource";
    public static final String TRUE = JavaModelManager.TRUE;
    protected static final ThreadLocal OPERATION_STACKS = new ThreadLocal();
    protected HashMap attributes;

    /**
     * Returns a status indicating if there is any known reason
     * this operation will fail.  Operations are verified before they
     * are run.
     *
     * Subclasses must override if they have any conditions to verify
     * before this operation executes.
     *
     * @see IJavaModelStatus
     */
    protected IJavaModelStatus verify() {
	return commonVerify();
    }

    /**
     * Returns &lt;code&gt;true&lt;/code&gt; if this operation performs no resource modifications,
     * otherwise &lt;code&gt;false&lt;/code&gt;. Subclasses must override.
     */
    public boolean isReadOnly() {
	return false;
    }

    /**
     * Runs this operation and registers any deltas created.
     *
     * @see IWorkspaceRunnable
     * @exception CoreException if the operation fails
     */
    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
	SubMonitor oldMonitor = this.progressMonitor;
	try {
	    JavaModelManager manager = JavaModelManager.getJavaModelManager();
	    DeltaProcessor deltaProcessor = manager.getDeltaProcessor();
	    int previousDeltaCount = deltaProcessor.javaModelDeltas.size();
	    try {
		this.progressMonitor = SubMonitor.convert(monitor);
		pushOperation(this);
		try {
		    if (canModifyRoots()) {
			// computes the root infos before executing the operation
			// noop if aready initialized
			JavaModelManager.getDeltaState().initializeRoots(false/*not initiAfterLoad*/);
		    }

		    executeOperation();
		} finally {
		    if (isTopLevelOperation()) {
			runPostActions();
		    }
		}
	    } finally {
		try {
		    // reacquire delta processor as it can have been reset during executeOperation()
		    deltaProcessor = manager.getDeltaProcessor();

		    // update JavaModel using deltas that were recorded during this operation
		    for (int i = previousDeltaCount, size = deltaProcessor.javaModelDeltas.size(); i &lt; size; i++) {
			deltaProcessor.updateJavaModel(deltaProcessor.javaModelDeltas.get(i));
		    }

		    // close the parents of the created elements and reset their project's cache (in case we are in an
		    // IWorkspaceRunnable and the clients wants to use the created element's parent)
		    // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=83646
		    for (int i = 0, length = this.resultElements.length; i &lt; length; i++) {
			IJavaElement element = this.resultElements[i];
			Openable openable = (Openable) element.getOpenable();
			if (!(openable instanceof CompilationUnit) || !((CompilationUnit) openable).isWorkingCopy()) { // a working copy must remain a child of its parent even after a move
			    ((JavaElement) openable.getParent()).close();
			}
			switch (element.getElementType()) {
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			case IJavaElement.PACKAGE_FRAGMENT:
			    deltaProcessor.projectCachesToReset.add(element.getJavaProject());
			    break;
			}
		    }
		    deltaProcessor.resetProjectCaches();

		    // fire only iff:
		    // - the operation is a top level operation
		    // - the operation did produce some delta(s)
		    // - but the operation has not modified any resource
		    if (isTopLevelOperation()) {
			if ((deltaProcessor.javaModelDeltas.size() &gt; previousDeltaCount
				|| !deltaProcessor.reconcileDeltas.isEmpty()) && !hasModifiedResource()) {
			    deltaProcessor.fire(null, DeltaProcessor.DEFAULT_CHANGE_EVENT);
			} // else deltas are fired while processing the resource delta
		    }
		} finally {
		    popOperation();
		}
	    }
	} finally {
	    if (monitor != null) {
		monitor.done();
	    }
	    this.progressMonitor = oldMonitor;
	}
    }

    protected ISchedulingRule getSchedulingRule() {
	return ResourcesPlugin.getWorkspace().getRoot();
    }

    /**
     * Common code used to verify the elements this operation is processing.
     * @see JavaModelOperation#verify()
     */
    protected IJavaModelStatus commonVerify() {
	if (this.elementsToProcess == null || this.elementsToProcess.length == 0) {
	    return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
	}
	for (int i = 0; i &lt; this.elementsToProcess.length; i++) {
	    if (this.elementsToProcess[i] == null) {
		return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
	    }
	}
	return JavaModelStatus.VERIFIED_OK;
    }

    protected void pushOperation(JavaModelOperation operation) {
	getCurrentOperationStack().add(operation);
    }

    protected boolean canModifyRoots() {
	return false;
    }

    /**
     * Performs the operation specific behavior. Subclasses must override.
     */
    protected abstract void executeOperation() throws JavaModelException;

    protected boolean isTopLevelOperation() {
	ArrayList stack;
	return (stack = getCurrentOperationStack()).size() &gt; 0 && stack.get(0) == this;
    }

    protected void runPostActions() throws JavaModelException {
	while (this.actionsStart &lt;= this.actionsEnd) {
	    IPostAction postAction = this.actions[this.actionsStart++];
	    if (POST_ACTION_VERBOSE) {
		System.out.println("(" + Thread.currentThread() //$NON-NLS-1$
			+ ") [JavaModelOperation.runPostActions()] Running action " + postAction.getID()); //$NON-NLS-1$
	    }
	    postAction.run();
	}
    }

    /**
     * Returns whether this operation has performed any resource modifications.
     * Returns false if this operation has not been executed yet.
     */
    public boolean hasModifiedResource() {
	return !isReadOnly() && getAttribute(HAS_MODIFIED_RESOURCE_ATTR) == TRUE;
    }

    protected JavaModelOperation popOperation() {
	ArrayList stack = getCurrentOperationStack();
	int size = stack.size();
	if (size &gt; 0) {
	    if (size == 1) { // top level operation
		OPERATION_STACKS.set(null); // release reference (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=33927)
	    }
	    return (JavaModelOperation) stack.remove(size - 1);
	} else {
	    return null;
	}
    }

    protected static ArrayList getCurrentOperationStack() {
	ArrayList stack = (ArrayList) OPERATION_STACKS.get();
	if (stack == null) {
	    stack = new ArrayList();
	    OPERATION_STACKS.set(stack);
	}
	return stack;
    }

    protected static Object getAttribute(Object key) {
	ArrayList stack = getCurrentOperationStack();
	if (stack.size() == 0)
	    return null;
	JavaModelOperation topLevelOp = (JavaModelOperation) stack.get(0);
	if (topLevelOp.attributes == null) {
	    return null;
	} else {
	    return topLevelOp.attributes.get(key);
	}
    }

    interface IPostAction {
	/**
	 * The progress monitor passed into this operation
	 */
	public SubMonitor progressMonitor = SubMonitor.convert(null);
	/**
	 * The elements created by this operation - empty
	 * until the operation actually creates elements.
	 */
	protected IJavaElement[] resultElements = NO_ELEMENTS;
	/**
	 * The elements this operation operates on,
	 * or &lt;code&gt;null&lt;/code&gt; if this operation
	 * does not operate on specific elements.
	 */
	protected IJavaElement[] elementsToProcess;
	protected int actionsStart = 0;
	protected int actionsEnd = -1;
	protected IPostAction[] actions;
	protected static boolean POST_ACTION_VERBOSE;
	public static final String HAS_MODIFIED_RESOURCE_ATTR = "hasModifiedResource";
	public static final String TRUE = JavaModelManager.TRUE;
	protected static final ThreadLocal OPERATION_STACKS = new ThreadLocal();
	protected HashMap attributes;

	String getID();

	void run() throws JavaModelException;

    }

}

