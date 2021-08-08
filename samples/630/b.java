import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.tree.JCTree.*;

class MemberEnter extends Visitor {
    /** Enter members from a list of trees.
     */
    void memberEnter(List&lt;? extends JCTree&gt; trees, Env&lt;AttrContext&gt; env) {
	for (List&lt;? extends JCTree&gt; l = trees; l.nonEmpty(); l = l.tail)
	    memberEnter(l.head, env);
    }

    /** Visitor argument: the current environment
     */
    protected Env&lt;AttrContext&gt; env;
    private final Check chk;

    /** Enter field and method definitions and process import
     *  clauses, catching any completion failure exceptions.
     */
    protected void memberEnter(JCTree tree, Env&lt;AttrContext&gt; env) {
	Env&lt;AttrContext&gt; prevEnv = this.env;
	try {
	    this.env = env;
	    tree.accept(this);
	} catch (CompletionFailure ex) {
	    chk.completionError(tree.pos(), ex);
	} finally {
	    this.env = prevEnv;
	}
    }

}

