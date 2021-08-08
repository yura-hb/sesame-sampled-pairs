abstract class SerializerBase implements SerializationHandler, SerializerConstants {
    /**
     * Return true if nothing has been sent to this result tree yet.
     * &lt;p&gt;
     * This is not a public API.
     *
     * @xsl.usage internal
     */
    public boolean documentIsEmpty() {
	// If we haven't called startDocument() yet, then this document is empty
	return m_docIsEmpty && (m_elemContext.m_currentElemDepth == 0);
    }

    boolean m_docIsEmpty = true;
    /**
     * A reference to "stack frame" corresponding to
     * the current element. Such a frame is pushed at a startElement()
     * and popped at an endElement(). This frame contains information about
     * the element, such as its namespace URI.
     */
    protected ElemContext m_elemContext = new ElemContext();

}

