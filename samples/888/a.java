class InferenceContext18 {
    /** Add new inference variables for the given type variables. */
    public InferenceVariable[] addTypeVariableSubstitutions(TypeBinding[] typeVariables) {
	int len2 = typeVariables.length;
	InferenceVariable[] newVariables = new InferenceVariable[len2];
	InferenceVariable[] toAdd = new InferenceVariable[len2];
	int numToAdd = 0;
	for (int i = 0; i &lt; typeVariables.length; i++) {
	    if (typeVariables[i] instanceof InferenceVariable)
		newVariables[i] = (InferenceVariable) typeVariables[i]; // prevent double substitution of an already-substituted inferenceVariable
	    else
		toAdd[numToAdd++] = newVariables[i] = InferenceVariable.get(typeVariables[i], i, this.currentInvocation,
			this.scope, this.object);
	}
	if (numToAdd &gt; 0) {
	    int start = 0;
	    if (this.inferenceVariables != null) {
		int len1 = this.inferenceVariables.length;
		System.arraycopy(this.inferenceVariables, 0,
			this.inferenceVariables = new InferenceVariable[len1 + numToAdd], 0, len1);
		start = len1;
	    } else {
		this.inferenceVariables = new InferenceVariable[numToAdd];
	    }
	    System.arraycopy(toAdd, 0, this.inferenceVariables, start, numToAdd);
	}
	return newVariables;
    }

    /** the invocation being inferred (for 18.5.1 and 18.5.2) */
    InvocationSite currentInvocation;
    Scope scope;
    ReferenceBinding object;
    /** The inference variables for which as solution is sought. */
    InferenceVariable[] inferenceVariables;

}

