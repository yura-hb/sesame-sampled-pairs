import com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.xni.QName;
import java.io.Writer;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

class XMLStreamWriterImpl extends AbstractMap&lt;Object, Object&gt; implements XMLStreamWriterBase {
    /**
     * @param target
     * @param data
     * @throws XMLStreamException
     */
    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
	try {
	    if (fStartTagOpened) {
		closeStartTag();
	    }

	    if ((target == null) || (data == null)) {
		throw new XMLStreamException("PI target cannot be null");
	    }

	    fWriter.write("&lt;?");
	    fWriter.write(target);
	    fWriter.write(SPACE);
	    fWriter.write(data);
	    fWriter.write("?&gt;");
	} catch (IOException e) {
	    throw new XMLStreamException(e);
	}
    }

    /**
     * Flag to track if start tag is opened
     */
    private boolean fStartTagOpened = false;
    /**
     * Underlying Writer to which characters are written.
     */
    private Writer fWriter;
    public static final String SPACE = " ";
    private ElementStack fElementStack = new ElementStack();
    /**
     * Flag for the value of repairNamespace property
     */
    private boolean fIsRepairingNamespace = false;
    /**
     * Collects namespace declarations when the writer is in reparing mode.
     */
    private List&lt;QName&gt; fNamespaceDecls;
    private NamespaceSupport fInternalNamespaceContext = null;
    /**
     * Collects attributes when the writer is in reparing mode.
     */
    private List&lt;Attribute&gt; fAttributeCache;
    /**
     * This is used to hold the namespace for attributes which happen to have
     * the same uri as the default namespace; It's added to avoid changing the
     * current impl. which has many redundant code for the repair mode
     */
    Map&lt;String, String&gt; fAttrNamespace = null;
    public static final String CLOSE_EMPTY_ELEMENT = "/&gt;";
    public static final char CLOSE_START_TAG = '&gt;';
    private SymbolTable fSymbolTable = new SymbolTable();
    /**
     * Namespace context encapsulating user specified context
     * and context built by the writer
     */
    private NamespaceContextImpl fNamespaceContext = null;
    private Random fPrefixGen = null;
    /**
     * In some cases, this charset encoder is used to determine if a char is
     * encodable by underlying writer. For example, an 8-bit char from the
     * extended ASCII set is not encodable by 7-bit ASCII encoder. Unencodable
     * chars are escaped using XML numeric entities.
     */
    private CharsetEncoder fEncoder = null;

    /**
     * marks close of start tag and writes the same into the writer.
     */
    private void closeStartTag() throws XMLStreamException {
	try {
	    ElementState currentElement = fElementStack.peek();

	    if (fIsRepairingNamespace) {
		repair();
		correctPrefix(currentElement, XMLStreamConstants.START_ELEMENT);

		if ((currentElement.prefix != null) && (currentElement.prefix != XMLConstants.DEFAULT_NS_PREFIX)) {
		    fWriter.write(currentElement.prefix);
		    fWriter.write(":");
		}

		fWriter.write(currentElement.localpart);

		int len = fNamespaceDecls.size();
		QName qname;

		for (int i = 0; i &lt; len; i++) {
		    qname = fNamespaceDecls.get(i);

		    if (qname != null) {
			if (fInternalNamespaceContext.declarePrefix(qname.prefix, qname.uri)) {
			    writenamespace(qname.prefix, qname.uri);
			}
		    }
		}

		fNamespaceDecls.clear();

		Attribute attr;

		for (int j = 0; j &lt; fAttributeCache.size(); j++) {
		    attr = fAttributeCache.get(j);

		    if ((attr.prefix != null) && (attr.uri != null)) {
			if (!attr.prefix.equals("") && !attr.uri.equals("")) {
			    String tmp = fInternalNamespaceContext.getPrefix(attr.uri);

			    if ((tmp == null) || (!tmp.equals(attr.prefix))) {
				tmp = getAttrPrefix(attr.uri);
				if (tmp == null) {
				    if (fInternalNamespaceContext.declarePrefix(attr.prefix, attr.uri)) {
					writenamespace(attr.prefix, attr.uri);
				    }
				} else {
				    writenamespace(attr.prefix, attr.uri);
				}
			    }
			}
		    }

		    writeAttributeWithPrefix(attr.prefix, attr.localpart, attr.value);
		}
		fAttrNamespace = null;
		fAttributeCache.clear();
	    }

	    if (currentElement.isEmpty) {
		fElementStack.pop();
		fInternalNamespaceContext.popContext();
		fWriter.write(CLOSE_EMPTY_ELEMENT);
	    } else {
		fWriter.write(CLOSE_START_TAG);
	    }

	    fStartTagOpened = false;
	} catch (IOException ex) {
	    fStartTagOpened = false;
	    throw new XMLStreamException(ex);
	}
    }

    /**
     * Correct's namespaces  as per requirements of isReparisingNamespace property.
     */
    protected void repair() {
	Attribute attr;
	Attribute attr2;
	ElementState currentElement = fElementStack.peek();
	removeDuplicateDecls();

	for (int i = 0; i &lt; fAttributeCache.size(); i++) {
	    attr = fAttributeCache.get(i);
	    if ((attr.prefix != null && !attr.prefix.equals("")) || (attr.uri != null && !attr.uri.equals(""))) {
		correctPrefix(currentElement, attr);
	    }
	}

	if (!isDeclared(currentElement)) {
	    if ((currentElement.prefix != null) && (currentElement.uri != null)) {
		if ((!currentElement.prefix.equals("")) && (!currentElement.uri.equals(""))) {
		    fNamespaceDecls.add(currentElement);
		}
	    }
	}

	for (int i = 0; i &lt; fAttributeCache.size(); i++) {
	    attr = fAttributeCache.get(i);
	    for (int j = i + 1; j &lt; fAttributeCache.size(); j++) {
		attr2 = fAttributeCache.get(j);
		if (!"".equals(attr.prefix) && !"".equals(attr2.prefix)) {
		    correctPrefix(attr, attr2);
		}
	    }
	}

	repairNamespaceDecl(currentElement);

	int i;

	for (i = 0; i &lt; fAttributeCache.size(); i++) {
	    attr = fAttributeCache.get(i);
	    /* If 'attr' is an attribute and it is in no namespace(which means that prefix="", uri=""), attr's
	       namespace should not be redinded. See [http://www.w3.org/TR/REC-xml-names/#defaulting].
	     */
	    if (attr.prefix != null && attr.prefix.equals("") && attr.uri != null && attr.uri.equals("")) {
		repairNamespaceDecl(attr);
	    }
	}

	QName qname = null;

	for (i = 0; i &lt; fNamespaceDecls.size(); i++) {
	    qname = fNamespaceDecls.get(i);

	    if (qname != null) {
		fInternalNamespaceContext.declarePrefix(qname.prefix, qname.uri);
	    }
	}

	for (i = 0; i &lt; fAttributeCache.size(); i++) {
	    attr = fAttributeCache.get(i);
	    correctPrefix(attr, XMLStreamConstants.ATTRIBUTE);
	}
    }

    /**
     *
     * @param uri
     * @return
     */
    private void correctPrefix(QName attr, int type) {
	String tmpPrefix;
	String prefix;
	String uri;
	prefix = attr.prefix;
	uri = attr.uri;
	boolean isSpecialCaseURI = false;

	if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
	    if (uri == null) {
		return;
	    }

	    if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix) && uri.equals(XMLConstants.DEFAULT_NS_PREFIX))
		return;

	    uri = fSymbolTable.addSymbol(uri);

	    QName decl;

	    for (int i = 0; i &lt; fNamespaceDecls.size(); i++) {
		decl = fNamespaceDecls.get(i);

		if ((decl != null) && (decl.uri.equals(attr.uri))) {
		    attr.prefix = decl.prefix;

		    return;
		}
	    }

	    tmpPrefix = fNamespaceContext.getPrefix(uri);

	    if (XMLConstants.DEFAULT_NS_PREFIX.equals(tmpPrefix)) {
		if (type == XMLStreamConstants.START_ELEMENT) {
		    return;
		} else if (type == XMLStreamConstants.ATTRIBUTE) {
		    //the uri happens to be the same as that of the default namespace
		    tmpPrefix = getAttrPrefix(uri);
		    isSpecialCaseURI = true;
		}
	    }

	    if (tmpPrefix == null) {
		StringBuilder genPrefix = new StringBuilder("zdef");

		for (int i = 0; i &lt; 1; i++) {
		    genPrefix.append(fPrefixGen.nextInt());
		}

		prefix = genPrefix.toString();
		prefix = fSymbolTable.addSymbol(prefix);
	    } else {
		prefix = fSymbolTable.addSymbol(tmpPrefix);
	    }

	    if (tmpPrefix == null) {
		if (isSpecialCaseURI) {
		    addAttrNamespace(prefix, uri);
		} else {
		    QName qname = new QName();
		    qname.setValues(prefix, XMLConstants.XMLNS_ATTRIBUTE, null, uri);
		    fNamespaceDecls.add(qname);
		    fInternalNamespaceContext.declarePrefix(fSymbolTable.addSymbol(prefix), uri);
		}
	    }
	}

	attr.prefix = prefix;
    }

    private void writenamespace(String prefix, String namespaceURI) throws IOException {
	fWriter.write(" xmlns");

	if ((prefix != null) && (!prefix.equals(XMLConstants.DEFAULT_NS_PREFIX))) {
	    fWriter.write(":");
	    fWriter.write(prefix);
	}

	fWriter.write("=\"");
	writeXMLContent(namespaceURI, true, // true = escapeChars
		true); // true = escapeDoubleQuotes
	fWriter.write("\"");
    }

    /**
     * return the prefix if the attribute has an uri the same as that of the default namespace
     */
    private String getAttrPrefix(String uri) {
	if (fAttrNamespace != null) {
	    return fAttrNamespace.get(uri);
	}
	return null;
    }

    private void writeAttributeWithPrefix(String prefix, String localName, String value) throws IOException {
	fWriter.write(SPACE);

	if ((prefix != null) && (!prefix.equals(XMLConstants.DEFAULT_NS_PREFIX))) {
	    fWriter.write(prefix);
	    fWriter.write(":");
	}

	fWriter.write(localName);
	fWriter.write("=\"");
	writeXMLContent(value, true, // true = escapeChars
		true); // true = escapeDoubleQuotes
	fWriter.write("\"");
    }

    void removeDuplicateDecls() {
	QName decl1, decl2;
	for (int i = 0; i &lt; fNamespaceDecls.size(); i++) {
	    decl1 = fNamespaceDecls.get(i);
	    if (decl1 != null) {
		for (int j = i + 1; j &lt; fNamespaceDecls.size(); j++) {
		    decl2 = fNamespaceDecls.get(j);
		    // QName.equals relies on identity equality, so we can't use it,
		    // because prefixes aren't interned
		    if (decl2 != null && decl1.prefix.equals(decl2.prefix) && decl1.uri.equals(decl2.uri))
			fNamespaceDecls.remove(j);
		}
	    }
	}
    }

    void correctPrefix(QName attr1, QName attr2) {
	String tmpPrefix;
	QName decl;

	checkForNull(attr1);
	checkForNull(attr2);

	if (attr1.prefix.equals(attr2.prefix) && !(attr1.uri.equals(attr2.uri))) {

	    tmpPrefix = fNamespaceContext.getPrefix(attr2.uri);

	    if (tmpPrefix != null) {
		attr2.prefix = fSymbolTable.addSymbol(tmpPrefix);
	    } else {
		for (int n = 0; n &lt; fNamespaceDecls.size(); n++) {
		    decl = fNamespaceDecls.get(n);
		    if (decl != null && (decl.uri.equals(attr2.uri))) {
			attr2.prefix = decl.prefix;

			return;
		    }
		}

		//No namespace mapping found , so declare prefix.
		StringBuilder genPrefix = new StringBuilder("zdef");

		for (int k = 0; k &lt; 1; k++) {
		    genPrefix.append(fPrefixGen.nextInt());
		}

		tmpPrefix = genPrefix.toString();
		tmpPrefix = fSymbolTable.addSymbol(tmpPrefix);
		attr2.prefix = tmpPrefix;

		QName qname = new QName();
		qname.setValues(tmpPrefix, XMLConstants.XMLNS_ATTRIBUTE, null, attr2.uri);
		fNamespaceDecls.add(qname);
	    }
	}
    }

    boolean isDeclared(QName attr) {
	QName decl;

	for (int n = 0; n &lt; fNamespaceDecls.size(); n++) {
	    decl = fNamespaceDecls.get(n);

	    if ((attr.prefix != null) && ((attr.prefix.equals(decl.prefix)) && (decl.uri.equals(attr.uri)))) {
		return true;
	    }
	}

	if (attr.uri != null) {
	    if (fNamespaceContext.getPrefix(attr.uri) != null) {
		return true;
	    }
	}

	return false;
    }

    void repairNamespaceDecl(QName attr) {
	QName decl;
	String tmpURI;

	//check for null prefix.
	for (int j = 0; j &lt; fNamespaceDecls.size(); j++) {
	    decl = fNamespaceDecls.get(j);

	    if (decl != null) {
		if ((attr.prefix != null) && (attr.prefix.equals(decl.prefix) && !(attr.uri.equals(decl.uri)))) {
		    tmpURI = fNamespaceContext.getNamespaceURI(attr.prefix);

		    //see if you need to add to symbole table.
		    if (tmpURI != null) {
			if (tmpURI.equals(attr.uri)) {
			    fNamespaceDecls.set(j, null);
			} else {
			    decl.uri = attr.uri;
			}
		    }
		}
	    }
	}
    }

    private void addAttrNamespace(String prefix, String uri) {
	if (fAttrNamespace == null) {
	    fAttrNamespace = new HashMap&lt;&gt;();
	}
	fAttrNamespace.put(prefix, uri);
    }

    /**
     * Writes XML content to underlying writer. Escapes characters unless
     * escaping character feature is turned off.
     */
    private void writeXMLContent(String content, boolean escapeChars, boolean escapeDoubleQuotes) throws IOException {

	if (!escapeChars) {
	    fWriter.write(content);

	    return;
	}

	// Index of the next char to be written
	int startWritePos = 0;

	final int end = content.length();

	for (int index = 0; index &lt; end; index++) {
	    char ch = content.charAt(index);

	    if (fEncoder != null && !fEncoder.canEncode(ch)) {
		fWriter.write(content, startWritePos, index - startWritePos);

		// Check if current and next characters forms a surrogate pair
		// and escape it to avoid generation of invalid xml content
		if (index != end - 1 && Character.isSurrogatePair(ch, content.charAt(index + 1))) {
		    writeCharRef(Character.toCodePoint(ch, content.charAt(index + 1)));
		    index++;
		} else {
		    writeCharRef(ch);
		}

		startWritePos = index + 1;
		continue;
	    }

	    switch (ch) {
	    case '&lt;':
		fWriter.write(content, startWritePos, index - startWritePos);
		fWriter.write("&lt;");
		startWritePos = index + 1;

		break;

	    case '&':
		fWriter.write(content, startWritePos, index - startWritePos);
		fWriter.write("&amp;");
		startWritePos = index + 1;

		break;

	    case '&gt;':
		fWriter.write(content, startWritePos, index - startWritePos);
		fWriter.write("&gt;");
		startWritePos = index + 1;

		break;

	    case '"':
		fWriter.write(content, startWritePos, index - startWritePos);
		if (escapeDoubleQuotes) {
		    fWriter.write("&quot;");
		} else {
		    fWriter.write('"');
		}
		startWritePos = index + 1;

		break;
	    }
	}

	// Write any pending data
	fWriter.write(content, startWritePos, end - startWritePos);
    }

    void checkForNull(QName attr) {
	if (attr.prefix == null)
	    attr.prefix = XMLConstants.DEFAULT_NS_PREFIX;
	if (attr.uri == null)
	    attr.uri = XMLConstants.DEFAULT_NS_PREFIX;
    }

    /**
     * Writes character reference in hex format.
     */
    private void writeCharRef(int codePoint) throws IOException {
	fWriter.write("&#x");
	fWriter.write(Integer.toHexString(codePoint));
	fWriter.write(';');
    }

    class ElementStack {
	/**
	* Flag to track if start tag is opened
	*/
	private boolean fStartTagOpened = false;
	/**
	* Underlying Writer to which characters are written.
	*/
	private Writer fWriter;
	public static final String SPACE = " ";
	private ElementStack fElementStack = new ElementStack();
	/**
	* Flag for the value of repairNamespace property
	*/
	private boolean fIsRepairingNamespace = false;
	/**
	* Collects namespace declarations when the writer is in reparing mode.
	*/
	private List&lt;QName&gt; fNamespaceDecls;
	private NamespaceSupport fInternalNamespaceContext = null;
	/**
	* Collects attributes when the writer is in reparing mode.
	*/
	private List&lt;Attribute&gt; fAttributeCache;
	/**
	* This is used to hold the namespace for attributes which happen to have
	* the same uri as the default namespace; It's added to avoid changing the
	* current impl. which has many redundant code for the repair mode
	*/
	Map&lt;String, String&gt; fAttrNamespace = null;
	public static final String CLOSE_EMPTY_ELEMENT = "/&gt;";
	public static final char CLOSE_START_TAG = '&gt;';
	private SymbolTable fSymbolTable = new SymbolTable();
	/**
	* Namespace context encapsulating user specified context
	* and context built by the writer
	*/
	private NamespaceContextImpl fNamespaceContext = null;
	private Random fPrefixGen = null;
	/**
	* In some cases, this charset encoder is used to determine if a char is
	* encodable by underlying writer. For example, an 8-bit char from the
	* extended ASCII set is not encodable by 7-bit ASCII encoder. Unencodable
	* chars are escaped using XML numeric entities.
	*/
	private CharsetEncoder fEncoder = null;

	/**
	 * This function is as a result of optimization done for endElement --
	 * we dont need to set the value for every end element we encouter.
	 * For Well formedness checks we can have the same QName object that was pushed.
	 * the values will be set only if application need to know about the endElement
	 */
	public ElementState peek() {
	    return fElements[fDepth - 1];
	}

	/**
	 * Pops an element off of the stack by setting the values of
	 * the specified QName.
	 * &lt;p&gt;
	 * &lt;strong&gt;Note:&lt;/strong&gt; The object returned is &lt;em&gt;not&lt;/em&gt;
	 * orphaned to the caller. Therefore, the caller should consider
	 * the object to be read-only.
	 */
	public ElementState pop() {
	    return fElements[--fDepth];
	}

    }

    class NamespaceContextImpl implements NamespaceContext {
	/**
	* Flag to track if start tag is opened
	*/
	private boolean fStartTagOpened = false;
	/**
	* Underlying Writer to which characters are written.
	*/
	private Writer fWriter;
	public static final String SPACE = " ";
	private ElementStack fElementStack = new ElementStack();
	/**
	* Flag for the value of repairNamespace property
	*/
	private boolean fIsRepairingNamespace = false;
	/**
	* Collects namespace declarations when the writer is in reparing mode.
	*/
	private List&lt;QName&gt; fNamespaceDecls;
	private NamespaceSupport fInternalNamespaceContext = null;
	/**
	* Collects attributes when the writer is in reparing mode.
	*/
	private List&lt;Attribute&gt; fAttributeCache;
	/**
	* This is used to hold the namespace for attributes which happen to have
	* the same uri as the default namespace; It's added to avoid changing the
	* current impl. which has many redundant code for the repair mode
	*/
	Map&lt;String, String&gt; fAttrNamespace = null;
	public static final String CLOSE_EMPTY_ELEMENT = "/&gt;";
	public static final char CLOSE_START_TAG = '&gt;';
	private SymbolTable fSymbolTable = new SymbolTable();
	/**
	* Namespace context encapsulating user specified context
	* and context built by the writer
	*/
	private NamespaceContextImpl fNamespaceContext = null;
	private Random fPrefixGen = null;
	/**
	* In some cases, this charset encoder is used to determine if a char is
	* encodable by underlying writer. For example, an 8-bit char from the
	* extended ASCII set is not encodable by 7-bit ASCII encoder. Unencodable
	* chars are escaped using XML numeric entities.
	*/
	private CharsetEncoder fEncoder = null;

	public String getPrefix(String uri) {
	    String prefix = null;

	    if (uri != null) {
		uri = fSymbolTable.addSymbol(uri);
	    }

	    if (internalContext != null) {
		prefix = internalContext.getPrefix(uri);

		if (prefix != null) {
		    return prefix;
		}
	    }

	    if (userContext != null) {
		return userContext.getPrefix(uri);
	    }

	    return null;
	}

	public String getNamespaceURI(String prefix) {
	    String uri = null;

	    if (prefix != null) {
		prefix = fSymbolTable.addSymbol(prefix);
	    }

	    if (internalContext != null) {
		uri = internalContext.getURI(prefix);

		if (uri != null) {
		    return uri;
		}
	    }

	    if (userContext != null) {
		uri = userContext.getNamespaceURI(prefix);

		return uri;
	    }

	    return null;
	}

    }

}

