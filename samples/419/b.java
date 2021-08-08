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

class Attr extends Visitor {
    /** Attribute a type argument list, returning a list of types.
     *  Check that all the types are references.
     */
    List&lt;Type&gt; attribTypes(List&lt;JCExpression&gt; trees, Env&lt;AttrContext&gt; env) {
	List&lt;Type&gt; types = attribAnyTypes(trees, env);
	return chk.checkRefTypes(trees, types);
    }

    final Check chk;
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

    /** Attribute a type argument list, returning a list of types.
     *  Caller is responsible for calling checkRefTypes.
     */
    List&lt;Type&gt; attribAnyTypes(List&lt;JCExpression&gt; trees, Env&lt;AttrContext&gt; env) {
	ListBuffer&lt;Type&gt; argtypes = new ListBuffer&lt;&gt;();
	for (List&lt;JCExpression&gt; l = trees; l.nonEmpty(); l = l.tail)
	    argtypes.append(attribType(l.head, env));
	return argtypes.toList();
    }

    /** Derived visitor method: attribute a type tree.
     */
    public Type attribType(JCTree tree, Env&lt;AttrContext&gt; env) {
	Type result = attribType(tree, env, Type.noType);
	return result;
    }

    /** Derived visitor method: attribute a type tree.
     */
    Type attribType(JCTree tree, Env&lt;AttrContext&gt; env, Type pt) {
	Type result = attribTree(tree, env, new ResultInfo(KindSelector.TYP, pt));
	return result;
    }

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

	ResultInfo(KindSelector pkind, Type pt) {
	    this(pkind, pt, chk.basicHandler, CheckMode.NORMAL);
	}

	protected ResultInfo(KindSelector pkind, Type pt, CheckContext checkContext, CheckMode checkMode) {
	    this.pkind = pkind;
	    this.pt = pt;
	    this.checkContext = checkContext;
	    this.checkMode = checkMode;
	}

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

