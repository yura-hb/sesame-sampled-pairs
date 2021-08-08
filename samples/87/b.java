import org.w3c.dom.Node;

class DOMUtil {
    /** Finds and returns the last child node with the given name. */
    public static Element getLastChildElement(Node parent, String elemNames[]) {

	// search for node
	Node child = parent.getLastChild();
	while (child != null) {
	    if (child.getNodeType() == Node.ELEMENT_NODE) {
		for (int i = 0; i &lt; elemNames.length; i++) {
		    if (child.getNodeName().equals(elemNames[i])) {
			return (Element) child;
		    }
		}
	    }
	    child = child.getPreviousSibling();
	}

	// not found
	return null;

    }

}

