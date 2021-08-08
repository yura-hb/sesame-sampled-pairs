import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.*;
import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.tree.JCTree.Tag.*;

class TreeInfo {
    /** Return the first call in a constructor definition. */
    public static JCMethodInvocation firstConstructorCall(JCTree tree) {
	if (!tree.hasTag(METHODDEF))
	    return null;
	JCMethodDecl md = (JCMethodDecl) tree;
	Names names = md.name.table.names;
	if (md.name != names.init)
	    return null;
	if (md.body == null)
	    return null;
	List&lt;JCStatement&gt; stats = md.body.stats;
	// Synthetic initializations can appear before the super call.
	while (stats.nonEmpty() && isSyntheticInit(stats.head))
	    stats = stats.tail;
	if (stats.isEmpty())
	    return null;
	if (!stats.head.hasTag(EXEC))
	    return null;
	JCExpressionStatement exec = (JCExpressionStatement) stats.head;
	if (!exec.expr.hasTag(APPLY))
	    return null;
	return (JCMethodInvocation) exec.expr;
    }

    /** Is statement an initializer for a synthetic field?
     */
    public static boolean isSyntheticInit(JCTree stat) {
	if (stat.hasTag(EXEC)) {
	    JCExpressionStatement exec = (JCExpressionStatement) stat;
	    if (exec.expr.hasTag(ASSIGN)) {
		JCAssign assign = (JCAssign) exec.expr;
		if (assign.lhs.hasTag(SELECT)) {
		    JCFieldAccess select = (JCFieldAccess) assign.lhs;
		    if (select.sym != null && (select.sym.flags() & SYNTHETIC) != 0) {
			Name selected = name(select.selected);
			if (selected != null && selected == selected.table.names._this)
			    return true;
		    }
		}
	    }
	}
	return false;
    }

    /** If this tree is an identifier or a field or a parameterized type,
     *  return its name, otherwise return null.
     */
    public static Name name(JCTree tree) {
	switch (tree.getTag()) {
	case IDENT:
	    return ((JCIdent) tree).name;
	case SELECT:
	    return ((JCFieldAccess) tree).name;
	case TYPEAPPLY:
	    return name(((JCTypeApply) tree).clazz);
	default:
	    return null;
	}
    }

}

