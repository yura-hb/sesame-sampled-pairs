import com.sun.org.apache.xpath.internal.XPathException;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

class XObject extends Expression implements Serializable, Cloneable {
    /**
    * Cast result object to a nodelist. Always issues an error.
    *
    * @return The object as a NodeSetDTM.
    *
    * @throws javax.xml.transform.TransformerException
    */
    public NodeSetDTM mutableNodeset() throws javax.xml.transform.TransformerException {

	error(XPATHErrorResources.ER_CANT_CONVERT_TO_MUTABLENODELIST, new Object[] { getTypeString() }); //"Can not convert "+getTypeString()+" to a NodeSetDTM!");

	return (NodeSetDTM) m_obj;
    }

    /**
    * The java object which this object wraps.
    *  @serial
    */
    protected Object m_obj;

    /**
    * Given a request type, return the equivalent string.
    * For diagnostic purposes.
    *
    * @return type string "#UNKNOWN" + object class name
    */
    public String getTypeString() {
	return "#UNKNOWN (" + object().getClass().getName() + ")";
    }

    /**
    * Tell the user of an error, and probably throw an
    * exception.
    *
    * @param msg Error message to issue
    * @param args Arguments to use in the message
    *
    * @throws javax.xml.transform.TransformerException
    */
    protected void error(String msg, Object[] args) throws javax.xml.transform.TransformerException {

	String fmsg = XSLMessages.createXPATHMessage(msg, args);

	// boolean shouldThrow = support.problem(m_support.XPATHPROCESSOR,
	//                                      m_support.ERROR,
	//                                      null,
	//                                      null, fmsg, 0, 0);
	// if(shouldThrow)
	{
	    throw new XPathException(fmsg, this);
	}
    }

    /**
    * Return a java object that's closest to the representation
    * that should be handed to an extension.
    *
    * @return The object that this class wraps
    */
    public Object object() {
	return m_obj;
    }

}

