import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase;
import com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2;
import org.xml.sax.Attributes;

class SAXImpl extends SAX2DTM2 implements DOMEnhancedForDTM, DOMBuilder {
    /**
     * SAX2: Receive notification of the beginning of an element.
     */
    public void startElement(String uri, String localName, String qname, Attributes attributes) throws SAXException {
	super.startElement(uri, localName, qname, attributes);

	handleTextEscaping();

	if (m_wsfilter != null) {
	    // Look for any xml:space attributes
	    // Depending on the implementation of attributes, this
	    // might be faster than looping through all attributes. ILENE
	    final int index = attributes.getIndex(XMLSPACE_STRING);
	    if (index &gt;= 0) {
		xmlSpaceDefine(attributes.getValue(index), m_parents.peek());
	    }
	}
    }

    private static final String XMLSPACE_STRING = "xml:space";
    private boolean _disableEscaping = false;
    private int _textNodeToProcess = DTM.NULL;
    private BitArray _dontEscape = null;
    private int _size = 0;
    private static final String PRESERVE_STRING = "preserve";
    private boolean _preserve = false;
    private int[] _xmlSpaceStack;
    private int _idx = 1;

    /**
     * Creates a text-node and checks if it is a whitespace node.
     */
    private void handleTextEscaping() {
	if (_disableEscaping && _textNodeToProcess != DTM.NULL && _type(_textNodeToProcess) == DTM.TEXT_NODE) {
	    if (_dontEscape == null) {
		_dontEscape = new BitArray(_size);
	    }

	    // Resize the _dontEscape BitArray if necessary.
	    if (_textNodeToProcess &gt;= _dontEscape.size()) {
		_dontEscape.resize(_dontEscape.size() * 2);
	    }

	    _dontEscape.setBit(_textNodeToProcess);
	    _disableEscaping = false;
	}
	_textNodeToProcess = DTM.NULL;
    }

    /**
     * Call this when an xml:space attribute is encountered to
     * define the whitespace strip/preserve settings.
     */
    private void xmlSpaceDefine(String val, final int node) {
	final boolean setting = val.equals(PRESERVE_STRING);
	if (setting != _preserve) {
	    _xmlSpaceStack[_idx++] = node;
	    _preserve = setting;
	}
    }

}

