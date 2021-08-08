import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.CaptureBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

class NullAnnotationMatching {
    /** Provided that both types are {@link TypeBinding#equalsEquals}, return the one that is more likely to show null at runtime. */
    public static TypeBinding moreDangerousType(TypeBinding one, TypeBinding two) {
	if (one == null)
	    return null;
	long oneNullBits = validNullTagBits(one.tagBits);
	long twoNullBits = validNullTagBits(two.tagBits);
	if (oneNullBits != twoNullBits) {
	    if (oneNullBits == TagBits.AnnotationNullable)
		return one; // nullable is dangerous
	    if (twoNullBits == TagBits.AnnotationNullable)
		return two; // nullable is dangerous
	    // below this point we have unknown vs. nonnull, which is which?
	    if (oneNullBits == 0)
		return one; // unknown is more dangerous than nonnull
	    return two; // unknown is more dangerous than nonnull
	} else if (one != two) { //$IDENTITY-COMPARISON$
	    if (analyse(one, two, -1).isAnyMismatch())
		return two; // two doesn't snugly fit into one, so it must be more dangerous
	}
	return one;
    }

    private final Severity severity;
    public static final NullAnnotationMatching NULL_ANNOTATIONS_OK = new NullAnnotationMatching(Severity.OK,
	    FlowInfo.UNKNOWN, null);
    public static final NullAnnotationMatching NULL_ANNOTATIONS_MISMATCH = new NullAnnotationMatching(Severity.MISMATCH,
	    FlowInfo.UNKNOWN, null);
    public static final NullAnnotationMatching NULL_ANNOTATIONS_OK_NONNULL = new NullAnnotationMatching(Severity.OK,
	    FlowInfo.NON_NULL, null);
    /** If non-null this field holds the supertype of the provided type which was used for direct matching. */
    public final TypeBinding superTypeHint;
    public final int nullStatus;

    public static long validNullTagBits(long bits) {
	bits &= TagBits.AnnotationNullMASK;
	return bits == TagBits.AnnotationNullMASK ? 0 : bits;
    }

    /**
     * Find any mismatches between the two given types, which are caused by null type annotations.
     * @param requiredType
     * @param providedType
     * @param nullStatus we are only interested in NULL or NON_NULL, -1 indicates that we are in a recursion, where flow info is ignored
     * @return a status object representing the severity of mismatching plus optionally a supertype hint
     */
    public static NullAnnotationMatching analyse(TypeBinding requiredType, TypeBinding providedType, int nullStatus) {
	return analyse(requiredType, providedType, null, null, nullStatus, null, CheckMode.COMPATIBLE);
    }

    public boolean isAnyMismatch() {
	return this.severity.isAnyMismatch();
    }

    /**
     * Find any mismatches between the two given types, which are caused by null type annotations.
     * @param requiredType
     * @param providedType
     * @param providedSubstitute in inheritance situations this maps the providedType into the realm of the subclass, needed for TVB identity checks.
     * 		Pass null if not interested in these added checks.
     * @param substitution TODO
     * @param nullStatus we are only interested in NULL or NON_NULL, -1 indicates that we are in a recursion, where flow info is ignored
     * @param providedExpression optionally holds the provided expression of type 'providedType'
     * @param mode controls the kind of check performed (see {@link CheckMode}).
     * @return a status object representing the severity of mismatching plus optionally a supertype hint
     */
    public static NullAnnotationMatching analyse(TypeBinding requiredType, TypeBinding providedType,
	    TypeBinding providedSubstitute, Substitution substitution, int nullStatus, Expression providedExpression,
	    CheckMode mode) {
	if (!requiredType.enterRecursiveFunction())
	    return NullAnnotationMatching.NULL_ANNOTATIONS_OK;
	try {
	    Severity severity = Severity.OK;
	    TypeBinding superTypeHint = null;
	    NullAnnotationMatching okStatus = NullAnnotationMatching.NULL_ANNOTATIONS_OK;
	    if (areSameTypes(requiredType, providedType, providedSubstitute)) {
		if ((requiredType.tagBits & TagBits.AnnotationNonNull) != 0)
		    return okNonNullStatus(providedExpression);
		return okStatus;
	    }
	    if (requiredType instanceof TypeVariableBinding && substitution != null && (mode == CheckMode.EXACT
		    || mode == CheckMode.COMPATIBLE || mode == CheckMode.BOUND_SUPER_CHECK)) {
		requiredType.exitRecursiveFunction();
		requiredType = Scope.substitute(substitution, requiredType);
		if (!requiredType.enterRecursiveFunction())
		    return NullAnnotationMatching.NULL_ANNOTATIONS_OK;
		if (areSameTypes(requiredType, providedType, providedSubstitute)) {
		    if ((requiredType.tagBits & TagBits.AnnotationNonNull) != 0)
			return okNonNullStatus(providedExpression);
		    return okStatus;
		}
	    }
	    if (mode == CheckMode.BOUND_CHECK && requiredType instanceof TypeVariableBinding) {
		boolean passedBoundCheck = (substitution instanceof ParameterizedTypeBinding)
			&& (((ParameterizedTypeBinding) substitution).tagBits & TagBits.PassedBoundCheck) != 0;
		if (!passedBoundCheck) {
		    // during bound check against a type variable check the provided type against all upper bounds:
		    TypeBinding superClass = requiredType.superclass();
		    if (superClass != null && (superClass.hasNullTypeAnnotations() || substitution != null)) { // annotations may enter when substituting a nested type variable
			NullAnnotationMatching status = analyse(superClass, providedType, null, substitution,
				nullStatus, providedExpression, CheckMode.BOUND_SUPER_CHECK);
			severity = severity.max(status.severity);
			if (severity == Severity.MISMATCH)
			    return new NullAnnotationMatching(severity, nullStatus, superTypeHint);
		    }
		    TypeBinding[] superInterfaces = requiredType.superInterfaces();
		    if (superInterfaces != null) {
			for (int i = 0; i &lt; superInterfaces.length; i++) {
			    if (superInterfaces[i].hasNullTypeAnnotations() || substitution != null) { // annotations may enter when substituting a nested type variable
				NullAnnotationMatching status = analyse(superInterfaces[i], providedType, null,
					substitution, nullStatus, providedExpression, CheckMode.BOUND_SUPER_CHECK);
				severity = severity.max(status.severity);
				if (severity == Severity.MISMATCH)
				    return new NullAnnotationMatching(severity, nullStatus, superTypeHint);
			    }
			}
		    }
		}
	    }
	    if (requiredType instanceof ArrayBinding) {
		long[] requiredDimsTagBits = ((ArrayBinding) requiredType).nullTagBitsPerDimension;
		if (requiredDimsTagBits != null) {
		    int dims = requiredType.dimensions();
		    if (requiredType.dimensions() == providedType.dimensions()) {
			long[] providedDimsTagBits = ((ArrayBinding) providedType).nullTagBitsPerDimension;
			if (providedDimsTagBits == null)
			    providedDimsTagBits = new long[dims + 1]; // set to unspec'd at all dimensions
			int currentNullStatus = nullStatus;
			for (int i = 0; i &lt;= dims; i++) {
			    long requiredBits = validNullTagBits(requiredDimsTagBits[i]);
			    long providedBits = validNullTagBits(providedDimsTagBits[i]);
			    if (i == 0 && requiredBits == TagBits.AnnotationNullable && nullStatus != -1
				    && mode.requiredNullableMatchesAll()) {
				// toplevel nullable array: no need to check 
				if (nullStatus == FlowInfo.NULL)
				    break; // null value has no details
			    } else {
				if (i &gt; 0)
				    currentNullStatus = -1; // don't use beyond the outermost dimension
				Severity dimSeverity = computeNullProblemSeverity(requiredBits, providedBits,
					currentNullStatus, i == 0 ? mode : mode.toDetail(), false);
				if (i &gt; 0 && dimSeverity == Severity.UNCHECKED
					&& providedExpression instanceof ArrayAllocationExpression && providedBits == 0
					&& requiredBits != 0) {
				    Expression[] dimensions = ((ArrayAllocationExpression) providedExpression).dimensions;
				    Expression previousDim = dimensions[i - 1];
				    if (previousDim instanceof IntLiteral && previousDim.constant.intValue() == 0) {
					dimSeverity = Severity.OK; // element of empty dimension matches anything
					nullStatus = -1;
					break;
				    }
				}
				severity = severity.max(dimSeverity);
				if (severity == Severity.MISMATCH)
				    return NullAnnotationMatching.NULL_ANNOTATIONS_MISMATCH;
			    }
			    if (severity == Severity.OK)
				nullStatus = -1;
			}
		    } else if (providedType.id == TypeIds.T_null) {
			if (dims &gt; 0 && requiredDimsTagBits[0] == TagBits.AnnotationNonNull)
			    return NullAnnotationMatching.NULL_ANNOTATIONS_MISMATCH;
		    }
		}
	    } else if (requiredType.hasNullTypeAnnotations() || providedType.hasNullTypeAnnotations()
		    || requiredType.isTypeVariable()) {
		long requiredBits = requiredNullTagBits(requiredType, mode);
		if (requiredBits == TagBits.AnnotationNullable && nullStatus != -1
			&& mode.requiredNullableMatchesAll()) {
		    // at toplevel (having a nullStatus) nullable matches all
		} else {
		    long providedBits = providedNullTagBits(providedType);
		    Severity s = computeNullProblemSeverity(requiredBits, providedBits, nullStatus, mode,
			    requiredType.isTypeVariable());
		    if (s.isAnyMismatch() && requiredType.isWildcard() && requiredBits != 0) {
			if (((WildcardBinding) requiredType).determineNullBitsFromDeclaration(null, null) == 0) {
			    // wildcard has its nullBits from the type variable: avoid redundant warning.
			    s = Severity.OK;
			}
		    }
		    severity = severity.max(s);
		    if (!severity.isAnyMismatch()
			    && (providedBits & TagBits.AnnotationNullMASK) == TagBits.AnnotationNonNull)
			okStatus = okNonNullStatus(providedExpression);
		}
		if (severity != Severity.MISMATCH && nullStatus != FlowInfo.NULL) { // null value has no details
		    TypeBinding providedSuper = providedType.findSuperTypeOriginatingFrom(requiredType);
		    TypeBinding providedSubstituteSuper = providedSubstitute != null
			    ? providedSubstitute.findSuperTypeOriginatingFrom(requiredType)
			    : null;
		    if (severity == Severity.UNCHECKED && requiredType.isTypeVariable() && providedType.isTypeVariable()
			    && (providedSuper == requiredType || providedSubstituteSuper == requiredType)) { //$IDENTITY-COMPARISON$
			severity = Severity.OK;
		    }
		    if (providedSuper != providedType) //$IDENTITY-COMPARISON$
			superTypeHint = providedSuper;
		    if (requiredType.isParameterizedType() && providedSuper instanceof ParameterizedTypeBinding) { // TODO(stephan): handle providedType.isRaw()
			TypeBinding[] requiredArguments = ((ParameterizedTypeBinding) requiredType).arguments;
			TypeBinding[] providedArguments = ((ParameterizedTypeBinding) providedSuper).arguments;
			TypeBinding[] providedSubstitutes = (providedSubstituteSuper instanceof ParameterizedTypeBinding)
				? ((ParameterizedTypeBinding) providedSubstituteSuper).arguments
				: null;
			if (requiredArguments != null && providedArguments != null
				&& requiredArguments.length == providedArguments.length) {
			    for (int i = 0; i &lt; requiredArguments.length; i++) {
				TypeBinding providedArgSubstitute = providedSubstitutes != null ? providedSubstitutes[i]
					: null;
				NullAnnotationMatching status = analyse(requiredArguments[i], providedArguments[i],
					providedArgSubstitute, substitution, -1, providedExpression, mode.toDetail());
				severity = severity.max(status.severity);
				if (severity == Severity.MISMATCH)
				    return new NullAnnotationMatching(severity, nullStatus, superTypeHint);
			    }
			}
		    }
		    TypeBinding requiredEnclosing = requiredType.enclosingType();
		    TypeBinding providedEnclosing = providedType.enclosingType();
		    if (requiredEnclosing != null && providedEnclosing != null) {
			TypeBinding providedEnclSubstitute = providedSubstitute != null
				? providedSubstitute.enclosingType()
				: null;
			NullAnnotationMatching status = analyse(requiredEnclosing, providedEnclosing,
				providedEnclSubstitute, substitution, -1, providedExpression, mode);
			severity = severity.max(status.severity);
		    }
		}
	    }
	    if (!severity.isAnyMismatch())
		return okStatus;
	    return new NullAnnotationMatching(severity, nullStatus, superTypeHint);
	} finally {
	    requiredType.exitRecursiveFunction();
	}
    }

    /** Are both types identical wrt the unannotated type and any null type annotations? Only unstructured types and captures are considered. */
    protected static boolean areSameTypes(TypeBinding requiredType, TypeBinding providedType,
	    TypeBinding providedSubstitute) {
	if (requiredType == providedType) //$IDENTITY-COMPARISON$ // short cut for really-really-same types
	    return true;
	if (requiredType.isParameterizedType() || requiredType.isArrayType())
	    return false; // not analysing details here
	if (TypeBinding.notEquals(requiredType, providedType)) {
	    if (requiredType instanceof CaptureBinding) {
		// when providing exactly the lower bound of the required type we're definitely fine:
		TypeBinding lowerBound = ((CaptureBinding) requiredType).lowerBound;
		if (lowerBound != null && areSameTypes(lowerBound, providedType, providedSubstitute))
		    return (requiredType.tagBits & TagBits.AnnotationNullMASK) == (providedType.tagBits
			    & TagBits.AnnotationNullMASK);
	    } else if (requiredType.kind() == Binding.TYPE_PARAMETER && requiredType == providedSubstitute) { //$IDENTITY-COMPARISON$
		return true;
	    } else if (providedType instanceof CaptureBinding) {
		// when requiring exactly the upper bound of the provided type we're fine, too:
		TypeBinding upperBound = ((CaptureBinding) providedType).upperBound();
		if (upperBound != null && areSameTypes(requiredType, upperBound, providedSubstitute))
		    return (requiredType.tagBits & TagBits.AnnotationNullMASK) == (providedType.tagBits
			    & TagBits.AnnotationNullMASK);
	    }
	    return false;
	}
	return (requiredType.tagBits & TagBits.AnnotationNullMASK) == (providedType.tagBits
		& TagBits.AnnotationNullMASK);
    }

    public static NullAnnotationMatching okNonNullStatus(final Expression providedExpression) {
	if (providedExpression instanceof MessageSend) {
	    final MethodBinding method = ((MessageSend) providedExpression).binding;
	    if (method != null && method.isValidBinding()) {
		MethodBinding originalMethod = method.original();
		TypeBinding originalDeclaringClass = originalMethod.declaringClass;
		if (originalDeclaringClass instanceof BinaryTypeBinding
			&& ((BinaryTypeBinding) originalDeclaringClass).externalAnnotationStatus
				.isPotentiallyUnannotatedLib()
			&& originalMethod.returnType.isTypeVariable()
			&& (originalMethod.returnType.tagBits & TagBits.AnnotationNullMASK) == 0) {
		    final int severity = ((BinaryTypeBinding) originalDeclaringClass).externalAnnotationStatus == ExternalAnnotationStatus.NO_EEA_FILE
			    ? ProblemSeverities.Warning
			    : ProblemSeverities.Info; // reduce severity if not configured to for external annotations
		    return new NullAnnotationMatching(Severity.LEGACY_WARNING, FlowInfo.UNKNOWN, null) {
			@Override
			public void report(Scope scope) {
			    scope.problemReporter().nonNullTypeVariableInUnannotatedBinary(scope.environment(), method,
				    providedExpression, severity);
			}
		    };
		}
	    }
	}
	return NullAnnotationMatching.NULL_ANNOTATIONS_OK_NONNULL;
    }

    NullAnnotationMatching(Severity severity, int nullStatus, TypeBinding superTypeHint) {
	this.severity = severity;
	this.superTypeHint = superTypeHint;
	this.nullStatus = nullStatus;
    }

    /**
     * Evaluate problem severity from the given details:
     * @param requiredBits null tagBits of the required type
     * @param providedBits null tagBits of the provided type
     * @param nullStatus -1 means: don't use, other values see constants in FlowInfo
     * @param mode check mode (see {@link CheckMode})
     * @param requiredIsTypeVariable is the required type a type variable (possibly: "free type variable")?
     * @return see {@link #severity} for interpretation of values
     */
    private static Severity computeNullProblemSeverity(long requiredBits, long providedBits, int nullStatus,
	    CheckMode mode, boolean requiredIsTypeVariable) {
	if (requiredBits == providedBits)
	    return Severity.OK;
	if (requiredBits == 0) {
	    switch (mode) {
	    case COMPATIBLE:
	    case BOUND_CHECK:
	    case BOUND_SUPER_CHECK:
	    case EXACT:
		return Severity.OK;
	    case OVERRIDE_RETURN:
		if (providedBits == TagBits.AnnotationNonNull)
		    return Severity.OK; // covariant redefinition to nonnull is good
		if (!requiredIsTypeVariable)
		    return Severity.OK; // refining an unconstrained non-TVB return to nullable is also legal
		return Severity.UNCHECKED;
	    case OVERRIDE:
		return Severity.UNCHECKED; // warn about dropped annotation
	    }
	} else if (requiredBits == TagBits.AnnotationNullMASK) {
	    return Severity.OK; // OK since LHS accepts either
	} else if (requiredBits == TagBits.AnnotationNonNull) {
	    switch (mode) {
	    case COMPATIBLE:
	    case BOUND_SUPER_CHECK:
		if (nullStatus == FlowInfo.NON_NULL)
		    return Severity.OK; // OK by flow analysis
		//$FALL-THROUGH$
	    case BOUND_CHECK:
	    case EXACT:
	    case OVERRIDE_RETURN:
	    case OVERRIDE:
		if (providedBits == 0)
		    return Severity.UNCHECKED;
		return Severity.MISMATCH;
	    }

	} else if (requiredBits == TagBits.AnnotationNullable) {
	    switch (mode) {
	    case COMPATIBLE:
	    case OVERRIDE_RETURN:
	    case BOUND_SUPER_CHECK:
		return Severity.OK; // in these modes everything is compatible to nullable
	    case BOUND_CHECK:
	    case EXACT:
		if (providedBits == 0)
		    return Severity.UNCHECKED;
		return Severity.MISMATCH;
	    case OVERRIDE:
		return Severity.MISMATCH;
	    }
	}
	return Severity.OK; // shouldn't get here, requiredBits should be one of the listed cases
    }

    static long requiredNullTagBits(TypeBinding type, CheckMode mode) {

	long tagBits = type.tagBits & TagBits.AnnotationNullMASK;
	if (tagBits != 0)
	    return validNullTagBits(tagBits);

	if (type.isWildcard()) {
	    return TagBits.AnnotationNullMASK;
	}

	if (type.isTypeVariable()) {
	    // assume we must require @NonNull, unless lower @Nullable bound
	    // (annotation directly on the TV has already been checked above)
	    if (type.isCapture()) {
		TypeBinding lowerBound = ((CaptureBinding) type).lowerBound;
		if (lowerBound != null) {
		    tagBits = lowerBound.tagBits & TagBits.AnnotationNullMASK;
		    if (tagBits == TagBits.AnnotationNullable)
			return TagBits.AnnotationNullable; // type cannot require @NonNull
		}
	    }
	    switch (mode) {
	    case BOUND_CHECK: // no pessimistic checks during boundcheck (we *have* the instantiation)
	    case BOUND_SUPER_CHECK:
	    case OVERRIDE: // no pessimistic checks during override check (comparing two *declarations*)
	    case OVERRIDE_RETURN:
		break;
	    default:
		return TagBits.AnnotationNonNull; // instantiation could require @NonNull
	    }
	}

	return 0;
    }

    static long providedNullTagBits(TypeBinding type) {

	long tagBits = type.tagBits & TagBits.AnnotationNullMASK;
	if (tagBits != 0)
	    return validNullTagBits(tagBits);

	if (type.isWildcard()) { // wildcard can be 'provided' during inheritance checks
	    return TagBits.AnnotationNullMASK;
	}

	if (type.isTypeVariable()) { // incl. captures
	    TypeVariableBinding typeVariable = (TypeVariableBinding) type;
	    boolean haveNullBits = false;
	    if (typeVariable.isCapture()) {
		TypeBinding lowerBound = ((CaptureBinding) typeVariable).lowerBound;
		if (lowerBound != null) {
		    tagBits = lowerBound.tagBits & TagBits.AnnotationNullMASK;
		    if (tagBits == TagBits.AnnotationNullable)
			return TagBits.AnnotationNullable; // cannot be @NonNull
		    haveNullBits |= (tagBits != 0);
		}
	    }
	    if (typeVariable.firstBound != null) {
		long boundBits = typeVariable.firstBound.tagBits & TagBits.AnnotationNullMASK;
		if (boundBits == TagBits.AnnotationNonNull)
		    return TagBits.AnnotationNonNull; // cannot be @Nullable
		haveNullBits |= (boundBits != 0);
	    }
	    if (haveNullBits)
		return TagBits.AnnotationNullMASK; // could be either, can only match to a wildcard accepting both
	}

	return 0;
    }

    class Severity extends Enum&lt;Severity&gt; {
	private final Severity severity;
	public static final NullAnnotationMatching NULL_ANNOTATIONS_OK = new NullAnnotationMatching(Severity.OK,
		FlowInfo.UNKNOWN, null);
	public static final NullAnnotationMatching NULL_ANNOTATIONS_MISMATCH = new NullAnnotationMatching(
		Severity.MISMATCH, FlowInfo.UNKNOWN, null);
	public static final NullAnnotationMatching NULL_ANNOTATIONS_OK_NONNULL = new NullAnnotationMatching(Severity.OK,
		FlowInfo.NON_NULL, null);
	/** If non-null this field holds the supertype of the provided type which was used for direct matching. */
	public final TypeBinding superTypeHint;
	public final int nullStatus;

	public boolean isAnyMismatch() {
	    return compareTo(LEGACY_WARNING) &gt; 0;
	}

	public Severity max(Severity severity) {
	    if (compareTo(severity) &lt; 0)
		return severity;
	    return this;
	}

    }

    class CheckMode extends Enum&lt;CheckMode&gt; {
	private final Severity severity;
	public static final NullAnnotationMatching NULL_ANNOTATIONS_OK = new NullAnnotationMatching(Severity.OK,
		FlowInfo.UNKNOWN, null);
	public static final NullAnnotationMatching NULL_ANNOTATIONS_MISMATCH = new NullAnnotationMatching(
		Severity.MISMATCH, FlowInfo.UNKNOWN, null);
	public static final NullAnnotationMatching NULL_ANNOTATIONS_OK_NONNULL = new NullAnnotationMatching(Severity.OK,
		FlowInfo.NON_NULL, null);
	/** If non-null this field holds the supertype of the provided type which was used for direct matching. */
	public final TypeBinding superTypeHint;
	public final int nullStatus;

	boolean requiredNullableMatchesAll() {
	    return false;
	}

	CheckMode toDetail() {
	    return CheckMode.EXACT;
	}

    }

}

