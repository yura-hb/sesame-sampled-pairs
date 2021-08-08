import java.awt.Checkbox;
import java.util.Hashtable;

class CheckboxOperator extends ComponentOperator implements Outputable {
    /**
     * Returns information about component.
     */
    @Override
    public Hashtable&lt;String, Object&gt; getDump() {
	Hashtable&lt;String, Object&gt; result = super.getDump();
	result.put(TEXT_DPROP, ((Checkbox) getSource()).getLabel());
	return result;
    }

    /**
     * Identifier for a label property.
     *
     * @see #getDump
     */
    public static final String TEXT_DPROP = "Label";

}

