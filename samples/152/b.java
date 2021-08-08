import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

class AttList implements Attributes {
    /**
    * Look up an attribute's value by name.
    *
    *
    * @param name The attribute node's name
    *
    * @return The attribute node's value
    */
    public String getValue(String name) {
	Attr attr = ((Attr) m_attrs.getNamedItem(name));
	return (null != attr) ? attr.getValue() : null;
    }

    /** List of attribute nodes          */
    NamedNodeMap m_attrs;

}

