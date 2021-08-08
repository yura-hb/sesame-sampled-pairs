import java.util.Map;
import org.xml.sax.helpers.AttributesImpl;

class AttributesImplSerializer extends AttributesImpl {
    /**
     * This method clears the accumulated attributes.
     *
     * @see org.xml.sax.helpers.AttributesImpl#clear()
     */
    public final void clear() {

	int len = super.getLength();
	super.clear();
	if (MAX &lt;= len) {
	    // if we have had enough attributes and are
	    // using the Map, then clear the Map too.
	    m_indexFromQName.clear();
	}

    }

    /**
     * This is the number of attributes before switching to the hash table,
     * and can be tuned, but 12 seems good for now - Brian M.
     */
    private static final int MAX = 12;
    /**
     * Hash table of qName/index values to quickly lookup the index
     * of an attributes qName.  qNames are in uppercase in the hash table
     * to make the search case insensitive.
     *
     * The keys to the hashtable to find the index are either
     * "prefix:localName"  or "{uri}localName".
     */
    private final Map&lt;String, Integer&gt; m_indexFromQName = new HashMap&lt;&gt;();

}

