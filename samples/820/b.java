import com.sun.org.apache.xml.internal.utils.NodeVector;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

class NodeSetDTM extends NodeVector implements DTMIterator, Cloneable {
    /**
    * Append the nodes to the list.
    *
    * @param nodes The nodes to be appended to this node set.
    * @throws RuntimeException thrown if this NodeSetDTM is not of
    * a mutable type.
    */
    public void appendNodes(NodeVector nodes) {

	if (!m_mutable)
	    throw new RuntimeException(
		    XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null)); //"This NodeSetDTM is not mutable!");

	super.appendNodes(nodes);
    }

    /** True if this list can be mutated.  */
    transient protected boolean m_mutable = true;

}

