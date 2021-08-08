import com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import javax.xml.stream.XMLStreamConstants;

class XMLStreamReaderImpl implements XMLStreamReader {
    /**
     * @param index
     * @return
     */
    public String getAttributeLocalName(int index) {
	//State should be either START_ELEMENT or ATTRIBUTE
	if (fEventType == XMLEvent.START_ELEMENT || fEventType == XMLEvent.ATTRIBUTE) {
	    return fScanner.getAttributeIterator().getLocalName(index);
	} else {
	    throw new java.lang.IllegalStateException();
	}
    }

    /**
     * current event type
     */
    private int fEventType;
    /**
     * Document scanner.
     */
    protected XMLDocumentScannerImpl fScanner = new XMLNSDocumentScannerImpl();

}

