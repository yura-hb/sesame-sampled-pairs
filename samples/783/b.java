import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMManager;
import com.sun.org.apache.xml.internal.utils.IntStack;

class XPathContext extends DTMManager {
    class XPathExpressionContext implements ExpressionContext {
	/**
	* Get the current context node.
	* @return The current context node.
	*/
	public org.w3c.dom.Node getContextNode() {
	    int context = getCurrentNode();

	    return getDTM(context).getNode(context);
	}

    }

    /** The stack of &lt;a href="http://www.w3.org/TR/xslt#dt-current-node"&gt;current node&lt;/a&gt; objects.
    *  Not to be confused with the current node list.  %REVIEW% Note that there
    *  are no bounds check and resize for this stack, so if it is blown, it's all
    *  over.  */
    private IntStack m_currentNodes = new IntStack(RECURSIONLIMIT);
    /**
    * Though XPathContext context extends
    * the DTMManager, it really is a proxy for this object, which
    * is the real DTMManager.
    */
    protected DTMManager m_dtmManager = null;

    /**
    * Get the current context node.
    *
    * @return the &lt;a href="http://www.w3.org/TR/xslt#dt-current-node"&gt;current node&lt;/a&gt;.
    */
    public final int getCurrentNode() {
	return m_currentNodes.peek();
    }

    /**
    * Get an instance of a DTM that "owns" a node handle.
    *
    * @param nodeHandle the nodeHandle.
    *
    * @return a non-null DTM reference.
    */
    public DTM getDTM(int nodeHandle) {
	return m_dtmManager.getDTM(nodeHandle);
    }

}

