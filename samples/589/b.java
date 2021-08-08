class EvaluationResult {
    /**
    * Returns whether there are errors in the code snippet or the global variable definition.
    */
    public boolean hasErrors() {
	if (this.problems == null) {
	    return false;
	} else {
	    for (int i = 0; i &lt; this.problems.length; i++) {
		if (this.problems[i].isError()) {
		    return true;
		}
	    }
	    return false;
	}
    }

    CategorizedProblem[] problems;

}

