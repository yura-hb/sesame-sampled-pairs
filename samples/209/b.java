class LambdaForm {
    /** Report the return type. */
    BasicType returnType() {
	if (result &lt; 0)
	    return V_TYPE;
	Name n = names[result];
	return n.type;
    }

    final int result;
    @Stable
    final Name[] names;

}

