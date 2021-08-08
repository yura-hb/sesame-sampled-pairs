import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.core.util.Util;

abstract class CreateElementInCUOperation extends JavaModelOperation {
    /**
     * Execute the operation - generate new source for the compilation unit
     * and save the results.
     *
     * @exception JavaModelException if the operation is unable to complete
     */
    @Override
    protected void executeOperation() throws JavaModelException {
	try {
	    beginTask(getMainTaskName(), getMainAmountOfWork());
	    JavaElementDelta delta = newJavaElementDelta();
	    ICompilationUnit unit = getCompilationUnit();
	    generateNewCompilationUnitAST(unit);
	    if (this.creationOccurred) {
		//a change has really occurred
		unit.save(null, false);
		boolean isWorkingCopy = unit.isWorkingCopy();
		if (!isWorkingCopy)
		    setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
		worked(1);
		this.resultElements = generateResultHandles();
		if (!isWorkingCopy // if unit is working copy, then save will have already fired the delta
			&& !Util.isExcluded(unit) && unit.getParent().exists()) {
		    for (int i = 0; i &lt; this.resultElements.length; i++) {
			delta.added(this.resultElements[i]);
		    }
		    addDelta(delta);
		} // else unit is created outside classpath
		  // non-java resource delta will be notified by delta processor
	    }
	} finally {
	    done();
	}
    }

    /**
     * A flag indicating whether creation of a new element occurred.
     * A request for creating a duplicate element would request in this
     * flag being set to &lt;code&gt;false&lt;/code&gt;. Ensures that no deltas are generated
     * when creation does not occur.
     */
    protected boolean creationOccurred = true;
    /**
     * The compilation unit AST used for this operation
     */
    protected CompilationUnit cuAST;
    /**
     * One of the position constants, describing where
     * to position the newly created element.
     */
    protected int insertionPolicy = INSERT_LAST;
    /**
     * A constant meaning to position the new element
     * before the element defined by &lt;code&gt;fAnchorElement&lt;/code&gt;.
     */
    protected static final int INSERT_BEFORE = 3;
    /**
     * The element that the newly created element is
     * positioned relative to, as described by
     * &lt;code&gt;fInsertPosition&lt;/code&gt;, or &lt;code&gt;null&lt;/code&gt;
     * if the newly created element will be positioned
     * last.
     */
    protected IJavaElement anchorElement = null;
    /**
     * A constant meaning to position the new element
     * after the element defined by &lt;code&gt;fAnchorElement&lt;/code&gt;.
     */
    protected static final int INSERT_AFTER = 2;
    /**
     * A constant meaning to position the new element
     * as the last child of its parent element.
     */
    protected static final int INSERT_LAST = 1;

    /**
     * Returns the name of the main task of this operation for
     * progress reporting.
     */
    public abstract String getMainTaskName();

    /**
     * Returns the amount of work for the main task of this operation for
     * progress reporting.
     */
    protected int getMainAmountOfWork() {
	return 2;
    }

    /**
     * Returns the compilation unit in which the new element is being created.
     */
    protected ICompilationUnit getCompilationUnit() {
	return getCompilationUnitFor(getParentElement());
    }

    protected void generateNewCompilationUnitAST(ICompilationUnit cu) throws JavaModelException {
	this.cuAST = parse(cu);

	AST ast = this.cuAST.getAST();
	ASTRewrite rewriter = ASTRewrite.create(ast);
	ASTNode child = generateElementAST(rewriter, cu);
	if (child != null) {
	    ASTNode parent = ((JavaElement) getParentElement()).findNode(this.cuAST);
	    if (parent == null)
		parent = this.cuAST;
	    insertASTNode(rewriter, parent, child);
	    TextEdit edits = rewriter.rewriteAST();
	    applyTextEdit(cu, edits);
	}
	worked(1);
    }

    /**
     * Creates and returns the handles for the elements this operation created.
     */
    protected IJavaElement[] generateResultHandles() {
	return new IJavaElement[] { generateResultHandle() };
    }

    protected CompilationUnit parse(ICompilationUnit cu) throws JavaModelException {
	// ensure cu is consistent (noop if already consistent)
	cu.makeConsistent(this.progressMonitor);
	// create an AST for the compilation unit
	ASTParser parser = ASTParser.newParser(AST.JLS11);
	parser.setSource(cu);
	return (CompilationUnit) parser.createAST(this.progressMonitor);
    }

    protected abstract ASTNode generateElementAST(ASTRewrite rewriter, ICompilationUnit cu) throws JavaModelException;

    /**
     * Inserts the given child into the given AST,
     * based on the position settings of this operation.
     *
     * @see #createAfter(IJavaElement)
     * @see #createBefore(IJavaElement)
     */
    protected void insertASTNode(ASTRewrite rewriter, ASTNode parent, ASTNode child) throws JavaModelException {
	StructuralPropertyDescriptor propertyDescriptor = getChildPropertyDescriptor(parent);
	if (propertyDescriptor instanceof ChildListPropertyDescriptor) {
	    ChildListPropertyDescriptor childListPropertyDescriptor = (ChildListPropertyDescriptor) propertyDescriptor;
	    ListRewrite rewrite = rewriter.getListRewrite(parent, childListPropertyDescriptor);
	    switch (this.insertionPolicy) {
	    case INSERT_BEFORE:
		ASTNode element = ((JavaElement) this.anchorElement).findNode(this.cuAST);
		if (childListPropertyDescriptor.getElementType().isAssignableFrom(element.getClass()))
		    rewrite.insertBefore(child, element, null);
		else
		    // case of an empty import list: the anchor element is the top level type and cannot be used in insertBefore as it is not the same type
		    rewrite.insertLast(child, null);
		break;
	    case INSERT_AFTER:
		element = ((JavaElement) this.anchorElement).findNode(this.cuAST);
		if (childListPropertyDescriptor.getElementType().isAssignableFrom(element.getClass()))
		    rewrite.insertAfter(child, element, null);
		else
		    // case of an empty import list: the anchor element is the top level type and cannot be used in insertAfter as it is not the same type
		    rewrite.insertLast(child, null);
		break;
	    case INSERT_LAST:
		rewrite.insertLast(child, null);
		break;
	    }
	} else {
	    rewriter.set(parent, propertyDescriptor, child, null);
	}
    }

    /**
     * Creates and returns the handle for the element this operation created.
     */
    protected abstract IJavaElement generateResultHandle();

    protected abstract StructuralPropertyDescriptor getChildPropertyDescriptor(ASTNode parent);

}

