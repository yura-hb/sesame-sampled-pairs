import org.eclipse.jdt.core.compiler.CharOperation;

class DOMField extends DOMMember implements IDOMField {
    /**
    * Returns the souce code to be used for this
    * field's type.
    */
    protected char[] getTypeContents() {
	if (isTypeAltered()) {
	    return this.fType.toCharArray();
	} else {
	    return CharOperation.subarray(this.fDocument, this.fTypeRange[0], this.fTypeRange[1] + 1);
	}
    }

    /**
     * Contains the type of the field when the type
     * has been altered from the contents in the
     * document, otherwise &lt;code&gt;null&lt;/code&gt;.
     */
    protected String fType;
    /**
     * The original inclusive source range of the
     * field's type in the document.
     */
    protected int[] fTypeRange;

    /**
    * Returns true if this field's type has been altered
    * from the original document contents.
    */
    protected boolean isTypeAltered() {
	return getMask(MASK_FIELD_TYPE_ALTERED);
    }

}

