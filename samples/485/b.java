import com.sun.tools.javac.util.*;
import com.sun.tools.javac.tree.JCTree.*;

class TreeScanner extends Visitor {
    /** Visitor method: scan a list of nodes.
     */
    public void scan(List&lt;? extends JCTree&gt; trees) {
	if (trees != null)
	    for (List&lt;? extends JCTree&gt; l = trees; l.nonEmpty(); l = l.tail)
		scan(l.head);
    }

    /** Visitor method: Scan a single node.
     */
    public void scan(JCTree tree) {
	if (tree != null)
	    tree.accept(this);
    }

}

