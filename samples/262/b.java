import com.sun.org.apache.xerces.internal.util.AugmentationsImpl;
import com.sun.org.apache.xerces.internal.xni.QName;
import java.util.Vector;

class DOMNormalizer implements XMLDocumentHandler {
    class XMLAttributesProxy implements XMLAttributes {
	/**
	 * This method adds default declarations
	         * @see com.sun.org.apache.xerces.internal.xni.XMLAttributes#addAttribute(QName, String, String)
	         */
	public int addAttribute(QName qname, String attrType, String attrValue) {
	    int index = fElement.getXercesAttribute(qname.uri, qname.localpart);
	    // add defaults to the tree
	    if (index &lt; 0) {
		// the default attribute was removed by a user and needed to
		// be added back
		AttrImpl attr = (AttrImpl) ((CoreDocumentImpl) fElement.getOwnerDocument()).createAttributeNS(qname.uri,
			qname.rawname, qname.localpart);
		// REVISIT: the following should also update ID table
		attr.setNodeValue(attrValue);
		index = fElement.setXercesAttributeNode(attr);
		fAugmentations.insertElementAt(new AugmentationsImpl(), index);
		attr.setSpecified(false);
	    } else {
		// default attribute is in the tree
		// we don't need to do anything since prefix was already fixed
		// at the namespace fixup time and value must be same value, otherwise
		// attribute will be treated as specified and we will never reach
		// this method.

	    }
	    return index;
	}

	protected ElementImpl fElement;
	protected final Vector&lt;Augmentations&gt; fAugmentations = new Vector&lt;&gt;(5);

    }

}

