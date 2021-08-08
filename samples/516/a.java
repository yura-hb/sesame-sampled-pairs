class InferenceContext18 {
    /** For use by 15.12.2.6 Method Invocation Type */
    public boolean usesUncheckedConversion() {
	return this.constraintsWithUncheckedConversion != null;
    }

    /** Signals whether any type compatibility makes use of unchecked conversion. */
    public List&lt;ConstraintFormula&gt; constraintsWithUncheckedConversion;

}

