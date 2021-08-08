import static com.sun.tools.javac.code.Type.*;
import static com.sun.tools.javac.code.TypeTag.*;

class Types {
    /**
     * The element type of an array.
     */
    public Type elemtype(Type t) {
	switch (t.getTag()) {
	case WILDCARD:
	    return elemtype(wildUpperBound(t));
	case ARRAY:
	    return ((ArrayType) t).elemtype;
	case FORALL:
	    return elemtype(((ForAll) t).qtype);
	case ERROR:
	    return t;
	default:
	    return null;
	}
    }

    final Symtab syms;

    /**
     * Get a wildcard's upper bound, returning non-wildcards unchanged.
     * @param t a type argument, either a wildcard or a type
     */
    public Type wildUpperBound(Type t) {
	if (t.hasTag(WILDCARD)) {
	    WildcardType w = (WildcardType) t;
	    if (w.isSuperBound())
		return w.bound == null ? syms.objectType : w.bound.bound;
	    else
		return wildUpperBound(w.type);
	} else
	    return t;
    }

}

