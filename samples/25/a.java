class JavaElementRequestor implements IJavaElementRequestor {
    /**
    * Reset the state of this requestor.
    */
    public void reset() {
	this.canceled = false;
	this.fields = null;
	this.initializers = null;
	this.memberTypes = null;
	this.methods = null;
	this.packageFragments = null;
	this.types = null;
    }

    /**
     * True if this requestor no longer wants to receive
     * results from its &lt;code&gt;IRequestorNameLookup&lt;/code&gt;.
     */
    protected boolean canceled = false;
    /**
     * A collection of the resulting fields, or &lt;code&gt;null&lt;/code&gt;
     * if no field results have been received.
     */
    protected ArrayList fields = null;
    /**
     * A collection of the resulting initializers, or &lt;code&gt;null&lt;/code&gt;
     * if no initializer results have been received.
     */
    protected ArrayList initializers = null;
    /**
     * A collection of the resulting member types, or &lt;code&gt;null&lt;/code&gt;
     * if no member type results have been received.
     */
    protected ArrayList memberTypes = null;
    /**
     * A collection of the resulting methods, or &lt;code&gt;null&lt;/code&gt;
     * if no method results have been received.
     */
    protected ArrayList methods = null;
    /**
     * A collection of the resulting package fragments, or &lt;code&gt;null&lt;/code&gt;
     * if no package fragment results have been received.
     */
    protected ArrayList packageFragments = null;
    /**
     * A collection of the resulting types, or &lt;code&gt;null&lt;/code&gt;
     * if no type results have been received.
     */
    protected ArrayList types = null;

}

