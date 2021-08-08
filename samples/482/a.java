class EvaluationResult {
    /**
    * Returns whether this result has a value.
    */
    public boolean hasValue() {
	return this.displayString != null;
    }

    char[] displayString;

}

