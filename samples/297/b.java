import com.sun.org.apache.xml.internal.dtm.DTM;

class OneStepIteratorForward extends ChildTestIterator {
    /**
    * Get the next node via getFirstAttribute && getNextAttribute.
    */
    protected int getNextNode() {
	m_lastFetched = (DTM.NULL == m_lastFetched) ? m_traverser.first(m_context)
		: m_traverser.next(m_context, m_lastFetched);
	return m_lastFetched;
    }

}

