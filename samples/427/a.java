class JavaElementDelta extends SimpleDelta implements IJavaElementDelta {
    /**
    * Clears the collection of affected children.
    */
    protected void clearAffectedChildren() {
	this.affectedChildren = EMPTY_DELTA;
	this.childIndex = null;
    }

    /**
     * @see #getAffectedChildren()
     */
    IJavaElementDelta[] affectedChildren = EMPTY_DELTA;
    /**
     * Empty array of IJavaElementDelta
     */
    static IJavaElementDelta[] EMPTY_DELTA = new IJavaElementDelta[] {};
    /**
     * On-demand index into affectedChildren
     */
    Map&lt;Key, Integer&gt; childIndex;

}

