import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM;
import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import com.sun.org.apache.xml.internal.dtm.DTM;
import java.util.HashMap;
import java.util.Map;

class KeyIndex extends DTMAxisIteratorBase {
    /**
     * Return an IntegerArray for the DOM Node which has the given id.
     *
     * @param id The id
     * @return A IntegerArray representing the Node whose id is the given value.
     */
    public IntegerArray getDOMNodeById(String id) {
	IntegerArray nodes = null;

	if (_enhancedDOM != null) {
	    int ident = _enhancedDOM.getElementById(id);

	    if (ident != DTM.NULL) {
		Integer root = _enhancedDOM.getDocument();
		Map&lt;String, IntegerArray&gt; index = _rootToIndexMap.get(root);

		if (index == null) {
		    index = new HashMap&lt;&gt;();
		    _rootToIndexMap.put(root, index);
		} else {
		    nodes = index.get(id);
		}

		if (nodes == null) {
		    nodes = new IntegerArray();
		    index.put(id, nodes);
		}

		nodes.add(_enhancedDOM.getNodeHandle(ident));
	    }
	}

	return nodes;
    }

    private DOMEnhancedForDTM _enhancedDOM;
    /**
     * A mapping from a document node to the mapping between values and nodesets
     */
    private Map&lt;Integer, Map&lt;String, IntegerArray&gt;&gt; _rootToIndexMap = new HashMap&lt;&gt;();

}

