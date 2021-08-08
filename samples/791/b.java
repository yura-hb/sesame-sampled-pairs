import java.util.EventObject;

class Notification extends EventObject {
    /**
     * Sets the source.
     *
     * @param source the new source for this object.
     *
     * @see EventObject#getSource
     */
    public void setSource(Object source) {
	super.source = source;
	this.source = source;
    }

    /**
     * &lt;p&gt;This field hides the {@link EventObject#source} field in the
     * parent class to make it non-transient and therefore part of the
     * serialized form.&lt;/p&gt;
     *
     * @serial The object on which the notification initially occurred.
     */
    protected Object source = null;

}

