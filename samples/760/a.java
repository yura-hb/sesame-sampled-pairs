import java.util.Set;

class InferenceContext18 {
    /** JLS 18.1.3 Bounds: throws α: the inference variable α appears in a throws clause */
    public void addThrowsContraints(TypeBinding[] parameters, InferenceVariable[] variables,
	    ReferenceBinding[] thrownExceptions) {
	for (int i = 0; i &lt; parameters.length; i++) {
	    TypeBinding parameter = parameters[i];
	    for (int j = 0; j &lt; thrownExceptions.length; j++) {
		if (TypeBinding.equalsEquals(parameter, thrownExceptions[j])) {
		    this.currentBounds.inThrows.add(variables[i].prototype());
		    break;
		}
	    }
	}
    }

    /** The accumulated type bounds etc. */
    BoundSet currentBounds;

}

