import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xpath.internal.Expression;

class UnionPathIterator extends LocPathIterator implements Cloneable, DTMIterator, Serializable, PathComponent {
    /**
    * Add an iterator to the union list.
    *
    * @param expr non-null reference to a location path iterator.
    */
    public void addIterator(DTMIterator expr) {

	// Increase array size by only 1 at a time.  Fix this
	// if it looks to be a problem.
	if (null == m_iterators) {
	    m_iterators = new DTMIterator[1];
	    m_iterators[0] = expr;
	} else {
	    DTMIterator[] exprs = m_iterators;
	    int len = m_iterators.length;

	    m_iterators = new DTMIterator[len + 1];

	    System.arraycopy(exprs, 0, m_iterators, 0, len);

	    m_iterators[len] = expr;
	}
	expr.nextNode();
	if (expr instanceof Expression)
	    ((Expression) expr).exprSetParent(this);
    }

    /**
    * The location path iterators, one for each
    * &lt;a href="http://www.w3.org/TR/xpath#NT-LocationPath"&gt;location
    * path&lt;/a&gt; contained in the union expression.
    * @serial
    */
    protected DTMIterator[] m_iterators;

}

