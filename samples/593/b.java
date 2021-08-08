import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Attribute.Compound;
import com.sun.tools.javac.comp.Annotate.AnnotationTypeMetadata;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree.*;
import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Flags.ANNOTATION;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.Kinds.Kind.*;
import static com.sun.tools.javac.code.TypeTag.*;

class Check {
    /** Is the annotation applicable to the symbol? */
    boolean annotationApplicable(JCAnnotation a, Symbol s) {
	Attribute.Array arr = getAttributeTargetAttribute(a.annotationType.type.tsym);
	Name[] targets;

	if (arr == null) {
	    targets = defaultTargetMetaInfo(a, s);
	} else {
	    // TODO: can we optimize this?
	    targets = new Name[arr.values.length];
	    for (int i = 0; i &lt; arr.values.length; ++i) {
		Attribute app = arr.values[i];
		if (!(app instanceof Attribute.Enum)) {
		    return true; // recovery
		}
		Attribute.Enum e = (Attribute.Enum) app;
		targets[i] = e.value.name;
	    }
	}
	for (Name target : targets) {
	    if (target == names.TYPE) {
		if (s.kind == TYP)
		    return true;
	    } else if (target == names.FIELD) {
		if (s.kind == VAR && s.owner.kind != MTH)
		    return true;
	    } else if (target == names.METHOD) {
		if (s.kind == MTH && !s.isConstructor())
		    return true;
	    } else if (target == names.PARAMETER) {
		if (s.kind == VAR && s.owner.kind == MTH && (s.flags() & PARAMETER) != 0) {
		    return true;
		}
	    } else if (target == names.CONSTRUCTOR) {
		if (s.kind == MTH && s.isConstructor())
		    return true;
	    } else if (target == names.LOCAL_VARIABLE) {
		if (s.kind == VAR && s.owner.kind == MTH && (s.flags() & PARAMETER) == 0) {
		    return true;
		}
	    } else if (target == names.ANNOTATION_TYPE) {
		if (s.kind == TYP && (s.flags() & ANNOTATION) != 0) {
		    return true;
		}
	    } else if (target == names.PACKAGE) {
		if (s.kind == PCK)
		    return true;
	    } else if (target == names.TYPE_USE) {
		if (s.kind == VAR && s.owner.kind == MTH && s.type.hasTag(NONE)) {
		    //cannot type annotate implictly typed locals
		    return false;
		} else if (s.kind == TYP || s.kind == VAR
			|| (s.kind == MTH && !s.isConstructor() && !s.type.getReturnType().hasTag(VOID))
			|| (s.kind == MTH && s.isConstructor())) {
		    return true;
		}
	    } else if (target == names.TYPE_PARAMETER) {
		if (s.kind == TYP && s.type.hasTag(TYPEVAR))
		    return true;
	    } else
		return true; // Unknown ElementType. This should be an error at declaration site,
			     // assume applicable.
	}
	return false;
    }

    private final Names names;
    private final Name[] dfltTargetMeta;

    Attribute.Array getAttributeTargetAttribute(TypeSymbol s) {
	Attribute.Compound atTarget = s.getAnnotationTypeMetadata().getTarget();
	if (atTarget == null)
	    return null; // ok, is applicable
	Attribute atValue = atTarget.member(names.value);
	if (!(atValue instanceof Attribute.Array))
	    return null; // error recovery
	return (Attribute.Array) atValue;
    }

    private Name[] defaultTargetMetaInfo(JCAnnotation a, Symbol s) {
	return dfltTargetMeta;
    }

}

