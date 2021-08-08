class ScriptFunction extends ScriptObject {
    /**
     * Get the prototype object for this function
     *
     * @return prototype
     */
    public final Object getPrototype() {
	if (prototype == LAZY_PROTOTYPE) {
	    prototype = new PrototypeObject(this);
	}
	return prototype;
    }

    /**
     * Reference to constructor prototype.
     */
    protected Object prototype;
    private static final Object LAZY_PROTOTYPE = new Object();

}

