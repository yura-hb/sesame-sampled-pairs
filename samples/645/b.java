import com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;

class XSSimpleTypeDecl implements XSSimpleType, TypeInfo {
    /**
     * [annotations]: a set of annotations for this simple type component if
     * it exists, otherwise an empty &lt;code&gt;XSObjectList&lt;/code&gt;.
     */
    public XSObjectList getAnnotations() {
	return (fAnnotations != null) ? fAnnotations : XSObjectListImpl.EMPTY_LIST;
    }

    private XSObjectList fAnnotations = null;

}

