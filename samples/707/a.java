import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;

class TypeDeclaration extends Statement implements ProblemSeverities, ReferenceContext {
    /**
     *	Common flow analysis for all types
     *
     */
    public void internalAnalyseCode(FlowContext flowContext, FlowInfo flowInfo) {

	if (this.binding.isPrivate() && !this.binding.isPrivateUsed()) {
	    if (!scope.referenceCompilationUnit().compilationResult.hasSyntaxError()) {
		scope.problemReporter().unusedPrivateType(this);
	    }
	}

	InitializationFlowContext initializerContext = new InitializationFlowContext(null, this, initializerScope);
	InitializationFlowContext staticInitializerContext = new InitializationFlowContext(null, this,
		staticInitializerScope);
	FlowInfo nonStaticFieldInfo = flowInfo.copy().unconditionalInits().discardFieldInitializations();
	FlowInfo staticFieldInfo = flowInfo.copy().unconditionalInits().discardFieldInitializations();
	if (fields != null) {
	    for (int i = 0, count = fields.length; i &lt; count; i++) {
		FieldDeclaration field = fields[i];
		if (field.isStatic()) {
		    /*if (field.isField()){
		    	staticInitializerContext.handledExceptions = NoExceptions; // no exception is allowed jls8.3.2
		    } else {*/
		    staticInitializerContext.handledExceptions = AnyException; // tolerate them all, and record them
		    /*}*/
		    staticFieldInfo = field.analyseCode(staticInitializerScope, staticInitializerContext,
			    staticFieldInfo);
		    // in case the initializer is not reachable, use a reinitialized flowInfo and enter a fake reachable
		    // branch, since the previous initializer already got the blame.
		    if (staticFieldInfo == FlowInfo.DEAD_END) {
			staticInitializerScope.problemReporter().initializerMustCompleteNormally(field);
			staticFieldInfo = FlowInfo.initial(maxFieldCount).setReachMode(FlowInfo.UNREACHABLE);
		    }
		} else {
		    /*if (field.isField()){
		    	initializerContext.handledExceptions = NoExceptions; // no exception is allowed jls8.3.2
		    } else {*/
		    initializerContext.handledExceptions = AnyException; // tolerate them all, and record them
		    /*}*/
		    nonStaticFieldInfo = field.analyseCode(initializerScope, initializerContext, nonStaticFieldInfo);
		    // in case the initializer is not reachable, use a reinitialized flowInfo and enter a fake reachable
		    // branch, since the previous initializer already got the blame.
		    if (nonStaticFieldInfo == FlowInfo.DEAD_END) {
			initializerScope.problemReporter().initializerMustCompleteNormally(field);
			nonStaticFieldInfo = FlowInfo.initial(maxFieldCount).setReachMode(FlowInfo.UNREACHABLE);
		    }
		}
	    }
	}
	if (memberTypes != null) {
	    for (int i = 0, count = memberTypes.length; i &lt; count; i++) {
		if (flowContext != null) { // local type
		    memberTypes[i].analyseCode(scope, flowContext,
			    nonStaticFieldInfo.copy().setReachMode(flowInfo.reachMode())); // reset reach mode in case initializers did abrupt completely
		} else {
		    memberTypes[i].analyseCode(scope);
		}
	    }
	}
	if (methods != null) {
	    UnconditionalFlowInfo outerInfo = flowInfo.copy().unconditionalInits().discardFieldInitializations();
	    FlowInfo constructorInfo = nonStaticFieldInfo.unconditionalInits().discardNonFieldInitializations()
		    .addInitializationsFrom(outerInfo);
	    for (int i = 0, count = methods.length; i &lt; count; i++) {
		AbstractMethodDeclaration method = methods[i];
		if (method.ignoreFurtherInvestigation)
		    continue;
		if (method.isInitializationMethod()) {
		    if (method.isStatic()) { // &lt;clinit&gt;
			method.analyseCode(scope, staticInitializerContext,
				staticFieldInfo.unconditionalInits().discardNonFieldInitializations()
					.addInitializationsFrom(outerInfo).setReachMode(flowInfo.reachMode())); // reset reach mode in case initializers did abrupt completely
		    } else { // constructor
			method.analyseCode(scope, initializerContext,
				constructorInfo.copy().setReachMode(flowInfo.reachMode())); // reset reach mode in case initializers did abrupt completely
		    }
		} else { // regular method
		    method.analyseCode(scope, null, flowInfo.copy());
		}
	    }
	}
    }

    public SourceTypeBinding binding;
    public ClassScope scope;
    public MethodScope initializerScope;
    public MethodScope staticInitializerScope;
    public FieldDeclaration[] fields;
    public int maxFieldCount;
    public TypeDeclaration[] memberTypes;
    public AbstractMethodDeclaration[] methods;
    public boolean ignoreFurtherInvestigation = false;

    /**
     *	Flow analysis for a local member innertype
     *
     */
    public void analyseCode(ClassScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

	if (ignoreFurtherInvestigation)
	    return;
	try {
	    if (flowInfo.isReachable()) {
		bits |= IsReachableMASK;
		LocalTypeBinding localType = (LocalTypeBinding) binding;
		localType.setConstantPoolName(currentScope.compilationUnitScope().computeConstantPoolName(localType));
	    }
	    manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
	    updateMaxFieldCount(); // propagate down the max field count
	    internalAnalyseCode(flowContext, flowInfo);
	} catch (AbortType e) {
	    this.ignoreFurtherInvestigation = true;
	}
    }

    /**
     *	Flow analysis for a member innertype
     *
     */
    public void analyseCode(ClassScope enclosingClassScope) {

	if (ignoreFurtherInvestigation)
	    return;
	try {
	    // propagate down the max field count
	    updateMaxFieldCount();
	    internalAnalyseCode(null, FlowInfo.initial(maxFieldCount));
	} catch (AbortType e) {
	    this.ignoreFurtherInvestigation = true;
	}
    }

    public void manageEnclosingInstanceAccessIfNecessary(ClassScope currentScope, FlowInfo flowInfo) {

	if (!flowInfo.isReachable())
	    return;
	NestedTypeBinding nestedType = (NestedTypeBinding) binding;
	nestedType.addSyntheticArgumentAndField(binding.enclosingType());
    }

    /**
     * MaxFieldCount's computation is necessary so as to reserve space for
     * the flow info field portions. It corresponds to the maximum amount of
     * fields this class or one of its innertypes have.
     *
     * During name resolution, types are traversed, and the max field count is recorded
     * on the outermost type. It is then propagated down during the flow analysis.
     *
     * This method is doing either up/down propagation.
     */
    void updateMaxFieldCount() {

	if (binding == null)
	    return; // error scenario
	TypeDeclaration outerMostType = scope.outerMostClassScope().referenceType();
	if (maxFieldCount &gt; outerMostType.maxFieldCount) {
	    outerMostType.maxFieldCount = maxFieldCount; // up
	} else {
	    maxFieldCount = outerMostType.maxFieldCount; // down
	}
    }

}

