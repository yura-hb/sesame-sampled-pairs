import org.w3c.dom.Document;
import org.w3c.dom.Node;

class LSSerializerImpl implements DOMConfiguration, LSSerializer {
    /**
     * Determines the XML Encoding of the Document Node to serialize.  If the Document Node
     * is not a DOM Level 3 Node, then the default encoding "UTF-8" is returned.
     *
     * @param  nodeArg The Node to serialize
     * @return A String containing the encoding pseudo-attribute of the XMLDecl.
     * @throws Throwable if the DOM implementation does not implement Document.getXmlEncoding()
     */
    protected String getXMLEncoding(Node nodeArg) {
	Document doc = null;

	// Determine the XML Encoding of the document
	if (nodeArg != null) {
	    if (nodeArg.getNodeType() == Node.DOCUMENT_NODE) {
		// The Document node is the Node argument
		doc = (Document) nodeArg;
	    } else {
		// The Document node is the Node argument's ownerDocument
		doc = nodeArg.getOwnerDocument();
	    }

	    // Determine the XML Version.
	    if (doc != null && doc.getImplementation().hasFeature("Core", "3.0")) {
		return doc.getXmlEncoding();
	    }
	}
	// The default encoding is UTF-8 except for the writeToString method
	return "UTF-8";
    }

}

