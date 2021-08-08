import java.util.*;
import java.io.*;

abstract class AbstractDocument implements Document, Serializable {
    /**
     * Gives a diagnostic dump.
     *
     * @param out the output stream
     */
    public void dump(PrintStream out) {
	Element root = getDefaultRootElement();
	if (root instanceof AbstractElement) {
	    ((AbstractElement) root).dump(out, 0);
	}
	bidiRoot.dump(out, 0);
    }

    /**
     * The root of the bidirectional structure for this document.  Its children
     * represent character runs with the same Unicode bidi level.
     */
    private transient BranchElement bidiRoot;
    /**
     * Name of the attribute used to specify element
     * names.
     */
    public static final String ElementNameAttribute = "$ename";
    /**
     * Where the text is actually stored, and a set of marks
     * that track change as the document is edited are managed.
     */
    private Content data;

    /**
     * Returns the root element that views should be based upon
     * unless some other mechanism for assigning views to element
     * structures is provided.
     *
     * @return the root element
     * @see Document#getDefaultRootElement
     */
    public abstract Element getDefaultRootElement();

    /**
     * Gets the content for the document.
     *
     * @return the content
     */
    protected final Content getContent() {
	return data;
    }

    abstract class AbstractElement implements Element, MutableAttributeSet, Serializable, TreeNode {
	/**
	* The root of the bidirectional structure for this document.  Its children
	* represent character runs with the same Unicode bidi level.
	*/
	private transient BranchElement bidiRoot;
	/**
	* Name of the attribute used to specify element
	* names.
	*/
	public static final String ElementNameAttribute = "$ename";
	/**
	* Where the text is actually stored, and a set of marks
	* that track change as the document is edited are managed.
	*/
	private Content data;

	/**
	 * Dumps a debugging representation of the element hierarchy.
	 *
	 * @param psOut the output stream
	 * @param indentAmount the indentation level &gt;= 0
	 */
	public void dump(PrintStream psOut, int indentAmount) {
	    PrintWriter out;
	    try {
		out = new PrintWriter(new OutputStreamWriter(psOut, "JavaEsc"), true);
	    } catch (UnsupportedEncodingException e) {
		out = new PrintWriter(psOut, true);
	    }
	    indent(out, indentAmount);
	    if (getName() == null) {
		out.print("&lt;??");
	    } else {
		out.print("&lt;" + getName());
	    }
	    if (getAttributeCount() &gt; 0) {
		out.println("");
		// dump the attributes
		Enumeration&lt;?&gt; names = attributes.getAttributeNames();
		while (names.hasMoreElements()) {
		    Object name = names.nextElement();
		    indent(out, indentAmount + 1);
		    out.println(name + "=" + getAttribute(name));
		}
		indent(out, indentAmount);
	    }
	    out.println("&gt;");

	    if (isLeaf()) {
		indent(out, indentAmount + 1);
		out.print("[" + getStartOffset() + "," + getEndOffset() + "]");
		Content c = getContent();
		try {
		    String contentStr = c.getString(getStartOffset(), getEndOffset() - getStartOffset())/*.trim()*/;
		    if (contentStr.length() &gt; 40) {
			contentStr = contentStr.substring(0, 40) + "...";
		    }
		    out.println("[" + contentStr + "]");
		} catch (BadLocationException e) {
		}

	    } else {
		int n = getElementCount();
		for (int i = 0; i &lt; n; i++) {
		    AbstractElement e = (AbstractElement) getElement(i);
		    e.dump(psOut, indentAmount + 1);
		}
	    }
	}

	private void indent(PrintWriter out, int n) {
	    for (int i = 0; i &lt; n; i++) {
		out.print("  ");
	    }
	}

	/**
	 * Gets the name of the element.
	 *
	 * @return the name, null if none
	 */
	public String getName() {
	    if (attributes.isDefined(ElementNameAttribute)) {
		return (String) attributes.getAttribute(ElementNameAttribute);
	    }
	    return null;
	}

	/**
	 * Gets the number of attributes that are defined.
	 *
	 * @return the number of attributes &gt;= 0
	 * @see AttributeSet#getAttributeCount
	 */
	public int getAttributeCount() {
	    return attributes.getAttributeCount();
	}

	/**
	 * Gets the value of an attribute.
	 *
	 * @param attrName the non-null attribute name
	 * @return the attribute value
	 * @see AttributeSet#getAttribute
	 */
	public Object getAttribute(Object attrName) {
	    Object value = attributes.getAttribute(attrName);
	    if (value == null) {
		// The delegate nor it's resolvers had a match,
		// so we'll try to resolve through the parent
		// element.
		AttributeSet a = (parent != null) ? parent.getAttributes() : null;
		if (a != null) {
		    value = a.getAttribute(attrName);
		}
	    }
	    return value;
	}

	/**
	 * Checks whether the element is a leaf.
	 *
	 * @return true if a leaf
	 */
	public abstract boolean isLeaf();

	/**
	 * Gets the starting offset in the model for the element.
	 *
	 * @return the offset &gt;= 0
	 */
	public abstract int getStartOffset();

	/**
	 * Gets the ending offset in the model for the element.
	 *
	 * @return the offset &gt;= 0
	 */
	public abstract int getEndOffset();

	/**
	 * Gets the number of children for the element.
	 *
	 * @return the number of children &gt;= 0
	 */
	public abstract int getElementCount();

	/**
	 * Gets a child element.
	 *
	 * @param index the child index, &gt;= 0 &amp;&amp; &lt; getElementCount()
	 * @return the child element
	 */
	public abstract Element getElement(int index);

    }

    interface Content {
	/**
	* The root of the bidirectional structure for this document.  Its children
	* represent character runs with the same Unicode bidi level.
	*/
	private transient BranchElement bidiRoot;
	/**
	* Name of the attribute used to specify element
	* names.
	*/
	public static final String ElementNameAttribute = "$ename";
	/**
	* Where the text is actually stored, and a set of marks
	* that track change as the document is edited are managed.
	*/
	private Content data;

	/**
	 * Fetches a string of characters contained in the sequence.
	 *
	 * @param where   Offset into the sequence to fetch &gt;= 0.
	 * @param len     number of characters to copy &gt;= 0.
	 * @return the string
	 * @exception BadLocationException  Thrown if the area covered by
	 *   the arguments is not contained in the character sequence.
	 */
	public String getString(int where, int len) throws BadLocationException;

    }

}

