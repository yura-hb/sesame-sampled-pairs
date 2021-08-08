import java.util.ArrayList;
import java.util.List;

class LiteralElement extends Instruction {
    /**
     * Set the first attribute of this element
     */
    public void setFirstAttribute(SyntaxTreeNode attribute) {
	if (_attributeElements == null) {
	    _attributeElements = new ArrayList&lt;&gt;(2);
	}
	_attributeElements.add(0, attribute);
    }

    private List&lt;SyntaxTreeNode&gt; _attributeElements = null;

}

