import com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl;

class XSDDescription extends XMLResourceIdentifierImpl implements XMLSchemaDescription {
    /**
     *  resets all the fields
     */
    public void reset() {
	super.clear();
	fContextType = CONTEXT_INITIALIZE;
	fLocationHints = null;
	fTriggeringComponent = null;
	fEnclosedElementName = null;
	fAttributes = null;
    }

    protected short fContextType;
    /**
     * Indicate that this description was just initialized.
     */
    public final static short CONTEXT_INITIALIZE = -1;
    protected String[] fLocationHints;
    protected QName fTriggeringComponent;
    protected QName fEnclosedElementName;
    protected XMLAttributes fAttributes;

}

