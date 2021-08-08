class SimplexTableau implements Serializable {
    /**
     * Get the offset of the first slack variable.
     * @return offset of the first slack variable
     */
    protected final int getSlackVariableOffset() {
	return getNumObjectiveFunctions() + numDecisionVariables;
    }

    /** Number of decision variables. */
    private final int numDecisionVariables;
    /** Number of artificial variables. */
    private int numArtificialVariables;

    /**
     * Get the number of objective functions in this tableau.
     * @return 2 for Phase 1.  1 for Phase 2.
     */
    protected final int getNumObjectiveFunctions() {
	return this.numArtificialVariables &gt; 0 ? 2 : 1;
    }

}

