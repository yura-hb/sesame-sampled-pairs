import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

class AxesWalker extends PredicatedNodeTest implements Cloneable, PathComponent, ExpressionOwner {
    /**
    * Set the root node of the TreeWalker.
    * (Not part of the DOM2 TreeWalker interface).
    *
    * @param root The context node of this step.
    */
    public void setRoot(int root) {
	// %OPT% Get this directly from the lpi.
	XPathContext xctxt = wi().getXPathContext();
	m_dtm = xctxt.getDTM(root);
	m_traverser = m_dtm.getAxisTraverser(m_axis);
	m_isFresh = true;
	m_foundLast = false;
	m_root = root;
	m_currentNode = root;

	if (DTM.NULL == root) {
	    throw new RuntimeException(
		    XSLMessages.createXPATHMessage(XPATHErrorResources.ER_SETTING_WALKER_ROOT_TO_NULL, null)); //"\n !!!! Error! Setting the root of a walker to null!!!");
	}

	resetProximityPositions();
    }

    /**
    * The DTM for the root.  This can not be used, or must be changed,
    * for the filter walker, or any walker that can have nodes
    * from multiple documents.
    * Never, ever, access this value without going through getDTM(int node).
    */
    private DTM m_dtm;
    /** The DTM inner traversal class, that corresponds to the super axis. */
    protected DTMAxisTraverser m_traverser;
    /** The traversal axis from where the nodes will be filtered. */
    protected int m_axis = -1;
    /** True if an itteration has not begun.  */
    transient boolean m_isFresh;
    /**
    *  The root node of the TreeWalker, as specified when it was created.
    */
    transient int m_root = DTM.NULL;
    /**
    *  The node at which the TreeWalker is currently positioned.
    */
    private transient int m_currentNode = DTM.NULL;

    public final WalkingIterator wi() {
	return (WalkingIterator) m_lpi;
    }

}

