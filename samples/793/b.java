import java.util.*;

class XMLEncoder extends Encoder implements AutoCloseable {
    /**
     * Sets the owner of this encoder to {@code owner}.
     *
     * @param owner The owner of this encoder.
     *
     * @see #getOwner
     */
    public void setOwner(Object owner) {
	this.owner = owner;
	writeExpression(new Expression(this, "getOwner", new Object[0]));
    }

    private Object owner;
    private boolean internal = false;
    private Map&lt;Object, ValueData&gt; valueToExpression;

    /**
     * Records the Expression so that the Encoder will
     * produce the actual output when the stream is flushed.
     * &lt;P&gt;
     * This method should only be invoked within the context of
     * initializing a persistence delegate or setting up an encoder to
     * read from a resource bundle.
     * &lt;P&gt;
     * For more information about using resource bundles with the
     * XMLEncoder, see
     * &lt;a href="http://www.oracle.com/technetwork/java/persistence4-140124.html#i18n"&gt;
     * Creating Internationalized Applications&lt;/a&gt;,
     *
     * @param oldExp The expression that will be written
     *               to the stream.
     * @see java.beans.PersistenceDelegate#initialize
     */
    public void writeExpression(Expression oldExp) {
	boolean internal = this.internal;
	this.internal = true;
	Object oldValue = getValue(oldExp);
	if (get(oldValue) == null || (oldValue instanceof String && !internal)) {
	    getValueData(oldValue).exp = oldExp;
	    super.writeExpression(oldExp);
	}
	this.internal = internal;
    }

    private ValueData getValueData(Object o) {
	ValueData d = valueToExpression.get(o);
	if (d == null) {
	    d = new ValueData();
	    valueToExpression.put(o, d);
	}
	return d;
    }

}

