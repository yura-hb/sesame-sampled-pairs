import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;

class DeepNodeListImpl implements NodeList {
    /** Returns the length of the node list. */
    public int getLength() {
	// Preload all matching elements. (Stops when we run out of subtree!)
	item(java.lang.Integer.MAX_VALUE);
	return nodes.size();
    }

    protected List&lt;Node&gt; nodes;
    protected NodeImpl rootNode;
    protected int changes = 0;
    protected boolean enableNS = false;
    protected String tagName;
    protected String nsName;

    /** Returns the node at the specified index. */
    public Node item(int index) {
	Node thisNode;

	// Tree changed. Do it all from scratch!
	if (rootNode.changes() != changes) {
	    nodes = new ArrayList&lt;&gt;();
	    changes = rootNode.changes();
	}

	// In the cache
	final int currentSize = nodes.size();
	if (index &lt; currentSize) {
	    return nodes.get(index);
	} // Not yet seen
	else {

	    // Pick up where we left off (Which may be the beginning)
	    if (currentSize == 0) {
		thisNode = rootNode;
	    } else {
		thisNode = (NodeImpl) (nodes.get(currentSize - 1));
	    }

	    // Add nodes up to the one we're looking for
	    while (thisNode != null && index &gt;= nodes.size()) {
		thisNode = nextMatchingElementAfter(thisNode);
		if (thisNode != null) {
		    nodes.add(thisNode);
		}
	    }

	    // Either what we want, or null (not avail.)
	    return thisNode;
	}

    }

    /**
     * Iterative tree-walker. When you have a Parent link, there's often no
     * need to resort to recursion. NOTE THAT only Element nodes are matched
     * since we're specifically supporting getElementsByTagName().
     */
    protected Node nextMatchingElementAfter(Node current) {

	Node next;
	while (current != null) {
	    // Look down to first child.
	    if (current.hasChildNodes()) {
		current = (current.getFirstChild());
	    } // Look right to sibling (but not from root!)
	    else if (current != rootNode && null != (next = current.getNextSibling())) {
		current = next;
	    } // Look up and right (but not past root!)
	    else {
		next = null;
		for (; current != rootNode; // Stop when we return to starting point
			current = current.getParentNode()) {

		    next = current.getNextSibling();
		    if (next != null) {
			break;
		    }
		}
		current = next;
	    }

	    // Have we found an Element with the right tagName?
	    // ("*" matches anything.)
	    if (current != rootNode && current != null && current.getNodeType() == Node.ELEMENT_NODE) {
		if (!enableNS) {
		    if (tagName.equals("*") || ((ElementImpl) current).getTagName().equals(tagName)) {
			return current;
		    }
		} else {
		    // DOM2: Namespace logic.
		    if (tagName.equals("*")) {
			if (nsName != null && nsName.equals("*")) {
			    return current;
			} else {
			    ElementImpl el = (ElementImpl) current;
			    if ((nsName == null && el.getNamespaceURI() == null)
				    || (nsName != null && nsName.equals(el.getNamespaceURI()))) {
				return current;
			    }
			}
		    } else {
			ElementImpl el = (ElementImpl) current;
			if (el.getLocalName() != null && el.getLocalName().equals(tagName)) {
			    if (nsName != null && nsName.equals("*")) {
				return current;
			    } else {
				if ((nsName == null && el.getNamespaceURI() == null)
					|| (nsName != null && nsName.equals(el.getNamespaceURI()))) {
				    return current;
				}
			    }
			}
		    }
		}
	    }

	    // Otherwise continue walking the tree
	}

	// Fell out of tree-walk; no more instances found
	return null;

    }

}

