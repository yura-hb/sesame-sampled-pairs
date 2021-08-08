import org.w3c.dom.Node;

class RangeImpl implements Range {
    /** is a an ancestor of b ? */
    boolean isAncestorOf(Node a, Node b) {
	for (Node node = b; node != null; node = node.getParentNode()) {
	    if (node == a)
		return true;
	}
	return false;
    }

}

