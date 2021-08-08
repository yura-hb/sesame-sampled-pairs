import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

class KeyIndex extends DTMAxisIteratorBase {
    class KeyIndexIterator extends MultiValuedNodeHeapIterator {
	/**
	 * Return the node at the given position.
	 *
	 * @param position The position
	 * @return The node at the given position.
	 */
	public int getNodeByPosition(int position) {
	    int node = DTMAxisIterator.END;

	    // If nodes are stored in _nodes, take advantage of the fact that
	    // there are no duplicates and they are stored in document order.
	    // Otherwise, fall back to the base heap implementation to do a
	    // good job with this.
	    if (_nodes != null) {
		if (position &gt; 0) {
		    if (position &lt;= _nodes.cardinality()) {
			_position = position;
			node = _nodes.at(position - 1);
		    } else {
			_position = _nodes.cardinality();
		    }
		}
	    } else {
		node = super.getNodeByPosition(position);
	    }

	    return node;
	}

	/**
	 * &lt;p&gt;A reference to the &lt;code&gt;key&lt;/code&gt; function that only has one
	 * key value or to the &lt;code&gt;id&lt;/code&gt; function that has only one string
	 * argument can be optimized to ignore the multi-valued heap.  This
	 * field will be &lt;code&gt;null&lt;/code&gt; otherwise.
	 */
	private IntegerArray _nodes;

    }

}

