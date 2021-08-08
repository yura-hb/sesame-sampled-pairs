import java.util.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.code.Symbol.*;
import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.Kinds.Kind.*;
import static com.sun.tools.javac.code.Scope.LookupKind.NON_RECURSIVE;
import static com.sun.tools.javac.code.TypeTag.*;

class Check {
    /** Check that classes (or interfaces) do not each define an abstract
     *  method with same name and arguments but incompatible return types.
     *  @param pos          Position to be used for error reporting.
     *  @param t1           The first argument type.
     *  @param t2           The second argument type.
     */
    public boolean checkCompatibleAbstracts(DiagnosticPosition pos, Type t1, Type t2, Type site) {
	if ((site.tsym.flags() & COMPOUND) != 0) {
	    // special case for intersections: need to eliminate wildcards in supertypes
	    t1 = types.capture(t1);
	    t2 = types.capture(t2);
	}
	return firstIncompatibility(pos, t1, t2, site) == null;
    }

    private final Types types;
    private final Log log;

    /** Return the first method which is defined with same args
     *  but different return types in two given interfaces, or null if none
     *  exists.
     *  @param t1     The first type.
     *  @param t2     The second type.
     *  @param site   The most derived type.
     *  @returns symbol from t2 that conflicts with one in t1.
     */
    private Symbol firstIncompatibility(DiagnosticPosition pos, Type t1, Type t2, Type site) {
	Map&lt;TypeSymbol, Type&gt; interfaces1 = new HashMap&lt;&gt;();
	closure(t1, interfaces1);
	Map&lt;TypeSymbol, Type&gt; interfaces2;
	if (t1 == t2)
	    interfaces2 = interfaces1;
	else
	    closure(t2, interfaces1, interfaces2 = new HashMap&lt;&gt;());

	for (Type t3 : interfaces1.values()) {
	    for (Type t4 : interfaces2.values()) {
		Symbol s = firstDirectIncompatibility(pos, t3, t4, site);
		if (s != null)
		    return s;
	    }
	}
	return null;
    }

    /** Compute all the supertypes of t, indexed by type symbol. */
    private void closure(Type t, Map&lt;TypeSymbol, Type&gt; typeMap) {
	if (!t.hasTag(CLASS))
	    return;
	if (typeMap.put(t.tsym, t) == null) {
	    closure(types.supertype(t), typeMap);
	    for (Type i : types.interfaces(t))
		closure(i, typeMap);
	}
    }

    /** Compute all the supertypes of t, indexed by type symbol (except thise in typesSkip). */
    private void closure(Type t, Map&lt;TypeSymbol, Type&gt; typesSkip, Map&lt;TypeSymbol, Type&gt; typeMap) {
	if (!t.hasTag(CLASS))
	    return;
	if (typesSkip.get(t.tsym) != null)
	    return;
	if (typeMap.put(t.tsym, t) == null) {
	    closure(types.supertype(t), typesSkip, typeMap);
	    for (Type i : types.interfaces(t))
		closure(i, typesSkip, typeMap);
	}
    }

    /** Return the first method in t2 that conflicts with a method from t1. */
    private Symbol firstDirectIncompatibility(DiagnosticPosition pos, Type t1, Type t2, Type site) {
	for (Symbol s1 : t1.tsym.members().getSymbols(NON_RECURSIVE)) {
	    Type st1 = null;
	    if (s1.kind != MTH || !s1.isInheritedIn(site.tsym, types) || (s1.flags() & SYNTHETIC) != 0)
		continue;
	    Symbol impl = ((MethodSymbol) s1).implementation(site.tsym, types, false);
	    if (impl != null && (impl.flags() & ABSTRACT) == 0)
		continue;
	    for (Symbol s2 : t2.tsym.members().getSymbolsByName(s1.name)) {
		if (s1 == s2)
		    continue;
		if (s2.kind != MTH || !s2.isInheritedIn(site.tsym, types) || (s2.flags() & SYNTHETIC) != 0)
		    continue;
		if (st1 == null)
		    st1 = types.memberType(t1, s1);
		Type st2 = types.memberType(t2, s2);
		if (types.overrideEquivalent(st1, st2)) {
		    List&lt;Type&gt; tvars1 = st1.getTypeArguments();
		    List&lt;Type&gt; tvars2 = st2.getTypeArguments();
		    Type rt1 = st1.getReturnType();
		    Type rt2 = types.subst(st2.getReturnType(), tvars2, tvars1);
		    boolean compat = types.isSameType(rt1, rt2)
			    || !rt1.isPrimitiveOrVoid() && !rt2.isPrimitiveOrVoid()
				    && (types.covariantReturnType(rt1, rt2, types.noWarnings)
					    || types.covariantReturnType(rt2, rt1, types.noWarnings))
			    || checkCommonOverriderIn(s1, s2, site);
		    if (!compat) {
			log.error(pos, Errors.TypesIncompatible(t1, t2,
				Fragments.IncompatibleDiffRet(s2.name, types.memberType(t2, s2).getParameterTypes())));
			return s2;
		    }
		} else if (checkNameClash((ClassSymbol) site.tsym, s1, s2) && !checkCommonOverriderIn(s1, s2, site)) {
		    log.error(pos, Errors.NameClashSameErasureNoOverride(s1.name,
			    types.memberType(site, s1).asMethodType().getParameterTypes(), s1.location(), s2.name,
			    types.memberType(site, s2).asMethodType().getParameterTypes(), s2.location()));
		    return s2;
		}
	    }
	}
	return null;
    }

    boolean checkCommonOverriderIn(Symbol s1, Symbol s2, Type site) {
	Map&lt;TypeSymbol, Type&gt; supertypes = new HashMap&lt;&gt;();
	Type st1 = types.memberType(site, s1);
	Type st2 = types.memberType(site, s2);
	closure(site, supertypes);
	for (Type t : supertypes.values()) {
	    for (Symbol s3 : t.tsym.members().getSymbolsByName(s1.name)) {
		if (s3 == s1 || s3 == s2 || s3.kind != MTH || (s3.flags() & (BRIDGE | SYNTHETIC)) != 0)
		    continue;
		Type st3 = types.memberType(site, s3);
		if (types.overrideEquivalent(st3, st1) && types.overrideEquivalent(st3, st2)
			&& types.returnTypeSubstitutable(st3, st1) && types.returnTypeSubstitutable(st3, st2)) {
		    return true;
		}
	    }
	}
	return false;
    }

    private boolean checkNameClash(ClassSymbol origin, Symbol s1, Symbol s2) {
	ClashFilter cf = new ClashFilter(origin.type);
	return (cf.accepts(s1) && cf.accepts(s2) && types.hasSameArgs(s1.erasure(types), s2.erasure(types)));
    }

    class ClashFilter implements Filter&lt;Symbol&gt; {
	private final Types types;
	private final Log log;

	ClashFilter(Type site) {
	    this.site = site;
	}

	public boolean accepts(Symbol s) {
	    return s.kind == MTH && (s.flags() & SYNTHETIC) == 0 && !shouldSkip(s) && s.isInheritedIn(site.tsym, types)
		    && !s.isConstructor();
	}

	boolean shouldSkip(Symbol s) {
	    return (s.flags() & CLASH) != 0 && s.owner == site.tsym;
	}

    }

}

