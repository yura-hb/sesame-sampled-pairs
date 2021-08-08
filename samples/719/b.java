import java.util.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Attribute.Compound;
import com.sun.tools.javac.comp.Annotate.AnnotationTypeMetadata;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree.*;
import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Flags.ANNOTATION;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.Kinds.Kind.*;
import static com.sun.tools.javac.code.TypeTag.*;
import static com.sun.tools.javac.tree.JCTree.Tag.*;

class Check {
    /** Check the annotations of a symbol.
     */
    public void validateAnnotations(List&lt;JCAnnotation&gt; annotations, Symbol s) {
	for (JCAnnotation a : annotations)
	    validateAnnotation(a, s);
    }

    private final Log log;
    private final Symtab syms;
    private final Names names;
    private final Name[] dfltTargetMeta;

    /** Check an annotation of a symbol.
     */
    private void validateAnnotation(JCAnnotation a, Symbol s) {
	validateAnnotationTree(a);

	if (a.type.tsym.isAnnotationType() && !annotationApplicable(a, s))
	    log.error(a.pos(), Errors.AnnotationTypeNotApplicable);

	if (a.annotationType.type.tsym == syms.functionalInterfaceType.tsym) {
	    if (s.kind != TYP) {
		log.error(a.pos(), Errors.BadFunctionalIntfAnno);
	    } else if (!s.isInterface() || (s.flags() & ANNOTATION) != 0) {
		log.error(a.pos(), Errors.BadFunctionalIntfAnno1(Fragments.NotAFunctionalIntf(s)));
	    }
	}
    }

    /**
     * Recursively validate annotations values
     */
    void validateAnnotationTree(JCTree tree) {
	class AnnotationValidator extends TreeScanner {
	    @Override
	    public void visitAnnotation(JCAnnotation tree) {
		if (!tree.type.isErroneous()) {
		    super.visitAnnotation(tree);
		    validateAnnotation(tree);
		}
	    }
	}
	tree.accept(new AnnotationValidator());
    }

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

    private boolean validateAnnotation(JCAnnotation a) {
	boolean isValid = true;
	AnnotationTypeMetadata metadata = a.annotationType.type.tsym.getAnnotationTypeMetadata();

	// collect an inventory of the annotation elements
	Set&lt;MethodSymbol&gt; elements = metadata.getAnnotationElements();

	// remove the ones that are assigned values
	for (JCTree arg : a.args) {
	    if (!arg.hasTag(ASSIGN))
		continue; // recovery
	    JCAssign assign = (JCAssign) arg;
	    Symbol m = TreeInfo.symbol(assign.lhs);
	    if (m == null || m.type.isErroneous())
		continue;
	    if (!elements.remove(m)) {
		isValid = false;
		log.error(assign.lhs.pos(), Errors.DuplicateAnnotationMemberValue(m.name, a.type));
	    }
	}

	// all the remaining ones better have default values
	List&lt;Name&gt; missingDefaults = List.nil();
	Set&lt;MethodSymbol&gt; membersWithDefault = metadata.getAnnotationElementsWithDefault();
	for (MethodSymbol m : elements) {
	    if (m.type.isErroneous())
		continue;

	    if (!membersWithDefault.contains(m))
		missingDefaults = missingDefaults.append(m.name);
	}
	missingDefaults = missingDefaults.reverse();
	if (missingDefaults.nonEmpty()) {
	    isValid = false;
	    Error errorKey = (missingDefaults.size() &gt; 1)
		    ? Errors.AnnotationMissingDefaultValue1(a.type, missingDefaults)
		    : Errors.AnnotationMissingDefaultValue(a.type, missingDefaults);
	    log.error(a.pos(), errorKey);
	}

	return isValid && validateTargetAnnotationValue(a);
    }

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

    boolean validateTargetAnnotationValue(JCAnnotation a) {
	// special case: java.lang.annotation.Target must not have
	// repeated values in its value member
	if (a.annotationType.type.tsym != syms.annotationTargetType.tsym || a.args.tail == null)
	    return true;

	boolean isValid = true;
	if (!a.args.head.hasTag(ASSIGN))
	    return false; // error recovery
	JCAssign assign = (JCAssign) a.args.head;
	Symbol m = TreeInfo.symbol(assign.lhs);
	if (m.name != names.value)
	    return false;
	JCTree rhs = assign.rhs;
	if (!rhs.hasTag(NEWARRAY))
	    return false;
	JCNewArray na = (JCNewArray) rhs;
	Set&lt;Symbol&gt; targets = new HashSet&lt;&gt;();
	for (JCTree elem : na.elems) {
	    if (!targets.add(TreeInfo.symbol(elem))) {
		isValid = false;
		log.error(elem.pos(), Errors.RepeatedAnnotationTarget);
	    }
	}
	return isValid;
    }

}

