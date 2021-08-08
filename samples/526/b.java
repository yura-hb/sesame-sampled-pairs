import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Scope.WriteableScope;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.comp.Check.CheckContext;
import com.sun.tools.javac.comp.DeferredAttr.AttrMode;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.List;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTag.*;

class Attr extends Visitor {
    /** Attribute the arguments in a method call, returning the method kind.
     */
    KindSelector attribArgs(KindSelector initialKind, List&lt;JCExpression&gt; trees, Env&lt;AttrContext&gt; env,
	    ListBuffer&lt;Type&gt; argtypes) {
	KindSelector kind = initialKind;
	for (JCExpression arg : trees) {
	    Type argtype = chk.checkNonVoid(arg, attribTree(arg, env, allowPoly ? methodAttrInfo : unknownExprInfo));
	    if (argtype.hasTag(DEFERRED)) {
		kind = KindSelector.of(KindSelector.POLY, kind);
	    }
	    argtypes.append(argtype);
	}
	return kind;
    }

    final Check chk;
    /** Switch: support target-typing inference
     */
    boolean allowPoly;
    final ResultInfo methodAttrInfo;
    final ResultInfo unknownExprInfo;
    /** Visitor argument: the current environment.
     */
    Env&lt;AttrContext&gt; env;
    /** Visitor argument: the currently expected attribution result.
     */
    ResultInfo resultInfo;
    /** Visitor result: the computed type.
     */
    Type result;
    final ArgumentAttr argumentAttr;
    private JCTree breakTree = null;
    final Symtab syms;

    /** Visitor method: attribute a tree, catching any completion failure
     *  exceptions. Return the tree's type.
     *
     *  @param tree    The tree to be visited.
     *  @param env     The environment visitor argument.
     *  @param resultInfo   The result info visitor argument.
     */
    Type attribTree(JCTree tree, Env&lt;AttrContext&gt; env, ResultInfo resultInfo) {
	Env&lt;AttrContext&gt; prevEnv = this.env;
	ResultInfo prevResult = this.resultInfo;
	try {
	    this.env = env;
	    this.resultInfo = resultInfo;
	    if (resultInfo.needsArgumentAttr(tree)) {
		result = argumentAttr.attribArg(tree, env);
	    } else {
		tree.accept(this);
	    }
	    if (tree == breakTree && resultInfo.checkContext.deferredAttrContext().mode == AttrMode.CHECK) {
		throw new BreakAttr(copyEnv(env));
	    }
	    return result;
	} catch (CompletionFailure ex) {
	    tree.type = syms.errType;
	    return chk.completionError(tree.pos(), ex);
	} finally {
	    this.env = prevEnv;
	    this.resultInfo = prevResult;
	}
    }

    Env&lt;AttrContext&gt; copyEnv(Env&lt;AttrContext&gt; env) {
	Env&lt;AttrContext&gt; newEnv = env.dup(env.tree, env.info.dup(copyScope(env.info.scope)));
	if (newEnv.outer != null) {
	    newEnv.outer = copyEnv(newEnv.outer);
	}
	return newEnv;
    }

    WriteableScope copyScope(WriteableScope sc) {
	WriteableScope newScope = WriteableScope.create(sc.owner);
	List&lt;Symbol&gt; elemsList = List.nil();
	for (Symbol sym : sc.getSymbols()) {
	    elemsList = elemsList.prepend(sym);
	}
	for (Symbol s : elemsList) {
	    newScope.enter(s);
	}
	return newScope;
    }

    class ResultInfo {
	final Check chk;
	/** Switch: support target-typing inference
	*/
	boolean allowPoly;
	final ResultInfo methodAttrInfo;
	final ResultInfo unknownExprInfo;
	/** Visitor argument: the current environment.
	*/
	Env&lt;AttrContext&gt; env;
	/** Visitor argument: the currently expected attribution result.
	*/
	ResultInfo resultInfo;
	/** Visitor result: the computed type.
	*/
	Type result;
	final ArgumentAttr argumentAttr;
	private JCTree breakTree = null;
	final Symtab syms;

	/**
	 * Should {@link Attr#attribTree} use the {@ArgumentAttr} visitor instead of this one?
	 * @param tree The tree to be type-checked.
	 * @return true if {@ArgumentAttr} should be used.
	 */
	protected boolean needsArgumentAttr(JCTree tree) {
	    return false;
	}

    }

    class BreakAttr extends RuntimeException {
	final Check chk;
	/** Switch: support target-typing inference
	*/
	boolean allowPoly;
	final ResultInfo methodAttrInfo;
	final ResultInfo unknownExprInfo;
	/** Visitor argument: the current environment.
	*/
	Env&lt;AttrContext&gt; env;
	/** Visitor argument: the currently expected attribution result.
	*/
	ResultInfo resultInfo;
	/** Visitor result: the computed type.
	*/
	Type result;
	final ArgumentAttr argumentAttr;
	private JCTree breakTree = null;
	final Symtab syms;

	private BreakAttr(Env&lt;AttrContext&gt; env) {
	    this.env = env;
	}

    }

}

