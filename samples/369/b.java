import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.tree.JCTree.*;
import static com.sun.tools.javac.tree.JCTree.Tag.*;

class Flow {
    class AliveAnalyzer extends BaseAnalyzer&lt;PendingExit&gt; {
	/** Analyze list of statements.
	 */
	void scanStats(List&lt;? extends JCStatement&gt; trees) {
	    if (trees != null)
		for (List&lt;? extends JCStatement&gt; l = trees; l.nonEmpty(); l = l.tail)
		    scanStat(l.head);
	}

	/** A flag that indicates whether the last statement could
	 *  complete normally.
	 */
	private boolean alive;

	/** Analyze a statement. Check that statement is reachable.
	 */
	void scanStat(JCTree tree) {
	    if (!alive && tree != null) {
		log.error(tree.pos(), Errors.UnreachableStmt);
		if (!tree.hasTag(SKIP))
		    alive = true;
	    }
	    scan(tree);
	}

    }

    private final Log log;

    abstract class BaseAnalyzer&lt;P&gt; extends TreeScanner {
	private final Log log;

	@Override
	public void scan(JCTree tree) {
	    if (tree != null && (tree.type == null || tree.type != Type.stuckType)) {
		super.scan(tree);
	    }
	}

    }

}

