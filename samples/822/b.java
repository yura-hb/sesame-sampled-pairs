import sun.tools.java.*;
import sun.tools.tree.*;

class SourceMember extends MemberDefinition implements Constants {
    /**
     * Get the initial value of the field
     */
    public Object getInitialValue() {
	if (isMethod() || (getValue() == null) || (!isFinal()) || (status != INLINED)) {
	    return null;
	}
	return ((Expression) getValue()).getValue();
    }

    /**
     * The status of the field
     */
    int status;
    static final int INLINED = 4;

}

