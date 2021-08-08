import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;

class CRTable implements CRTFlags {
    class SourceComputer extends Visitor {
	/** The start position of given tree.
	 */
	public int startPos(JCTree tree) {
	    if (tree == null)
		return Position.NOPOS;
	    return TreeInfo.getStartPos(tree);
	}

    }

}

