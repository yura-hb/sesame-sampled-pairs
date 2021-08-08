import org.w3c.dom.Node;

class XMLUtils {
    /**
     * @param sibling
     * @param nodeName
     * @param number
     * @return nodes with the constrain
     */
    public static Text selectDs11NodeText(Node sibling, String nodeName, int number) {
	Node n = selectDs11Node(sibling, nodeName, number);
	if (n == null) {
	    return null;
	}
	n = n.getFirstChild();
	while (n != null && n.getNodeType() != Node.TEXT_NODE) {
	    n = n.getNextSibling();
	}
	return (Text) n;
    }

    /**
     * @param sibling
     * @param nodeName
     * @param number
     * @return nodes with the constraint
     */
    public static Element selectDs11Node(Node sibling, String nodeName, int number) {
	while (sibling != null) {
	    if (Constants.SignatureSpec11NS.equals(sibling.getNamespaceURI())
		    && sibling.getLocalName().equals(nodeName)) {
		if (number == 0) {
		    return (Element) sibling;
		}
		number--;
	    }
	    sibling = sibling.getNextSibling();
	}
	return null;
    }

}

