import sun.tools.java.*;

class Statement extends Node {
    /**
     * Set the label of a statement
     */
    public void setLabel(Environment env, Expression e) {
	if (e.op == IDENT) {
	    if (labels == null) {
		labels = new Identifier[1];
	    } else {
		// this should almost never happen.  Multiple labels on
		// the same statement.  But handle it gracefully.
		Identifier newLabels[] = new Identifier[labels.length + 1];
		System.arraycopy(labels, 0, newLabels, 1, labels.length);
		labels = newLabels;
	    }
	    labels[0] = ((IdentifierExpression) e).id;
	} else {
	    env.error(e.where, "invalid.label");
	}
    }

    Identifier labels[] = null;

}

