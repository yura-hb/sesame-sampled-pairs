import java.util.*;

class RPUtils {
    /**
     * Scan for leaves accumulating
     * the nodes in the passed in list
     * @param nodes the nodes so far
     */
    public static void scanForLeaves(List&lt;RPNode&gt; nodes, RPNode current) {
	if (current.getLeft() == null && current.getRight() == null)
	    nodes.add(current);
	if (current.getLeft() != null)
	    scanForLeaves(nodes, current.getLeft());
	if (current.getRight() != null)
	    scanForLeaves(nodes, current.getRight());
    }

    /**
     * Scan for leaves accumulating
     * the nodes in the passed in list
     * @param nodes the nodes so far
     * @param scan the tree to scan
     */
    public static void scanForLeaves(List&lt;RPNode&gt; nodes, RPTree scan) {
	scanForLeaves(nodes, scan.getRoot());
    }

}

