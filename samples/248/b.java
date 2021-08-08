import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;
import com.sun.tools.javac.comp.Infer.IncorporationAction;
import com.sun.tools.javac.util.*;
import static com.sun.tools.javac.code.TypeTag.*;

abstract class Type extends AnnoConstruct implements TypeMirror {
    class UndetVar extends DelegatedType {
	/**
	 * Returns a new copy of this undet var.
	 */
	public UndetVar dup(Types types) {
	    UndetVar uv2 = new UndetVar((TypeVar) qtype, listener, types);
	    dupTo(uv2, types);
	    return uv2;
	}

	/** inference variable's change listener */
	public UndetVarListener listener = null;
	Kind kind;
	/** inference variable bounds */
	protected Map&lt;InferenceBound, List&lt;Type&gt;&gt; bounds;
	/** number of declared (upper) bounds */
	public int declaredCount;
	/** inference variable's inferred type (set from Infer.java) */
	private Type inst = null;
	/** list of incorporation actions (used by the incorporation engine). */
	public ArrayDeque&lt;IncorporationAction&gt; incorporationActions = new ArrayDeque&lt;&gt;();
	TypeMapping&lt;Void&gt; toTypeVarMap = new StructuralTypeMapping&lt;Void&gt;() {
	    @Override
	    public Type visitUndetVar(UndetVar uv, Void _unused) {
		return uv.inst != null ? uv.inst : uv.qtype;
	    }
	};

	public UndetVar(TypeVar origin, UndetVarListener listener, Types types) {
	    // This is a synthesized internal type, so we cannot annotate it.
	    super(UNDETVAR, origin);
	    this.kind = origin.isCaptured() ? Kind.CAPTURED : Kind.NORMAL;
	    this.listener = listener;
	    bounds = new EnumMap&lt;&gt;(InferenceBound.class);
	    List&lt;Type&gt; declaredBounds = types.getBounds(origin);
	    declaredCount = declaredBounds.length();
	    bounds.put(InferenceBound.UPPER, List.nil());
	    bounds.put(InferenceBound.LOWER, List.nil());
	    bounds.put(InferenceBound.EQ, List.nil());
	    for (Type t : declaredBounds.reverse()) {
		//add bound works in reverse order
		addBound(InferenceBound.UPPER, t, types, true);
	    }
	    if (origin.isCaptured() && !origin.lower.hasTag(BOT)) {
		//add lower bound if needed
		addBound(InferenceBound.LOWER, origin.lower, types, true);
	    }
	}

	/**
	 * Dumps the contents of this undet var on another undet var.
	 */
	public void dupTo(UndetVar uv2, Types types) {
	    uv2.listener = null;
	    uv2.bounds.clear();
	    for (InferenceBound ib : InferenceBound.values()) {
		uv2.bounds.put(ib, List.nil());
		for (Type t : getBounds(ib)) {
		    uv2.addBound(ib, t, types, true);
		}
	    }
	    uv2.inst = inst;
	    uv2.listener = listener;
	    uv2.incorporationActions = new ArrayDeque&lt;&gt;();
	    for (IncorporationAction action : incorporationActions) {
		uv2.incorporationActions.add(action.dup(uv2));
	    }
	}

	@SuppressWarnings("fallthrough")
	private void addBound(InferenceBound ib, Type bound, Types types, boolean update) {
	    if (kind == Kind.CAPTURED && !update) {
		//Captured inference variables bounds must not be updated during incorporation,
		//except when some inference variable (beta) has been instantiated in the
		//right-hand-side of a 'C&lt;alpha&gt; = capture(C&lt;? extends/super beta&gt;) constraint.
		if (bound.hasTag(UNDETVAR) && !((UndetVar) bound).isCaptured()) {
		    //If the new incoming bound is itself a (regular) inference variable,
		    //then we are allowed to propagate this inference variable bounds to it.
		    ((UndetVar) bound).addBound(ib.complement(), this, types, false);
		}
	    } else {
		Type bound2 = bound.map(toTypeVarMap).baseType();
		List&lt;Type&gt; prevBounds = bounds.get(ib);
		if (bound == qtype)
		    return;
		for (Type b : prevBounds) {
		    //check for redundancy - do not add same bound twice
		    if (types.isSameType(b, bound2))
			return;
		}
		bounds.put(ib, prevBounds.prepend(bound2));
		notifyBoundChange(ib, bound2, false);
	    }
	}

	/** get all bounds of a given kind */
	public List&lt;Type&gt; getBounds(InferenceBound... ibs) {
	    ListBuffer&lt;Type&gt; buf = new ListBuffer&lt;&gt;();
	    for (InferenceBound ib : ibs) {
		buf.appendList(bounds.get(ib));
	    }
	    return buf.toList();
	}

	public final boolean isCaptured() {
	    return kind == Kind.CAPTURED;
	}

	private void notifyBoundChange(InferenceBound ib, Type bound, boolean update) {
	    if (listener != null) {
		listener.varBoundChanged(this, ib, bound, update);
	    }
	}

	class InferenceBound extends Enum&lt;InferenceBound&gt; {
	    /** inference variable's change listener */
	    public UndetVarListener listener = null;
	    Kind kind;
	    /** inference variable bounds */
	    protected Map&lt;InferenceBound, List&lt;Type&gt;&gt; bounds;
	    /** number of declared (upper) bounds */
	    public int declaredCount;
	    /** inference variable's inferred type (set from Infer.java) */
	    private Type inst = null;
	    /** list of incorporation actions (used by the incorporation engine). */
	    public ArrayDeque&lt;IncorporationAction&gt; incorporationActions = new ArrayDeque&lt;&gt;();
	    TypeMapping&lt;Void&gt; toTypeVarMap = new StructuralTypeMapping&lt;Void&gt;() {
		@Override
		public Type visitUndetVar(UndetVar uv, Void _unused) {
		    return uv.inst != null ? uv.inst : uv.qtype;
		}
	    };

	    public abstract InferenceBound complement();

	}

	interface UndetVarListener {
	    /** inference variable's change listener */
	    public UndetVarListener listener = null;
	    Kind kind;
	    /** inference variable bounds */
	    protected Map&lt;InferenceBound, List&lt;Type&gt;&gt; bounds;
	    /** number of declared (upper) bounds */
	    public int declaredCount;
	    /** inference variable's inferred type (set from Infer.java) */
	    private Type inst = null;
	    /** list of incorporation actions (used by the incorporation engine). */
	    public ArrayDeque&lt;IncorporationAction&gt; incorporationActions = new ArrayDeque&lt;&gt;();
	    TypeMapping&lt;Void&gt; toTypeVarMap = new StructuralTypeMapping&lt;Void&gt;() {
		@Override
		public Type visitUndetVar(UndetVar uv, Void _unused) {
		    return uv.inst != null ? uv.inst : uv.qtype;
		}
	    };

	    /** called when some inference variable bounds (of given kinds ibs) change */
	    void varBoundChanged(UndetVar uv, InferenceBound ib, Type bound, boolean update);

	}

    }

    /** The defining class / interface / package / type variable.
     */
    public TypeSymbol tsym;
    /**
     * Type metadata,  Should be {@code null} for the default value.
     *
     * Note: it is an invariant that for any {@code TypeMetadata}
     * class, a given {@code Type} may have at most one metadata array
     * entry of that class.
     */
    protected final TypeMetadata metadata;

    /**
     * Checks if the current type tag is equal to the given tag.
     * @return true if tag is equal to the current type tag.
     */
    public boolean hasTag(TypeTag tag) {
	return tag == getTag();
    }

    /** map a type function over all immediate descendants of this type (no arg version)
     */
    public &lt;Z&gt; Type map(TypeMapping&lt;Z&gt; mapping) {
	return mapping.visit(this, null);
    }

    /**
     * If this is a constant type, return its underlying type.
     * Otherwise, return the type itself.
     */
    public Type baseType() {
	return this;
    }

    /**
     * Returns the current type tag.
     * @return the value of the current type tag.
     */
    public abstract TypeTag getTag();

    /** Define a type given its tag, type symbol, and type annotations
     */

    public Type(TypeSymbol tsym, TypeMetadata metadata) {
	Assert.checkNonNull(metadata);
	this.tsym = tsym;
	this.metadata = metadata;
    }

    abstract class DelegatedType extends Type {
	/** The defining class / interface / package / type variable.
	*/
	public TypeSymbol tsym;
	/**
	* Type metadata,  Should be {@code null} for the default value.
	*
	* Note: it is an invariant that for any {@code TypeMetadata}
	* class, a given {@code Type} may have at most one metadata array
	* entry of that class.
	*/
	protected final TypeMetadata metadata;

	public DelegatedType(TypeTag tag, Type qtype) {
	    this(tag, qtype, TypeMetadata.EMPTY);
	}

	public DelegatedType(TypeTag tag, Type qtype, TypeMetadata metadata) {
	    super(qtype.tsym, metadata);
	    this.tag = tag;
	    this.qtype = qtype;
	}

    }

    class TypeVar extends Type implements TypeVariable {
	/** The defining class / interface / package / type variable.
	*/
	public TypeSymbol tsym;
	/**
	* Type metadata,  Should be {@code null} for the default value.
	*
	* Note: it is an invariant that for any {@code TypeMetadata}
	* class, a given {@code Type} may have at most one metadata array
	* entry of that class.
	*/
	protected final TypeMetadata metadata;

	public boolean isCaptured() {
	    return false;
	}

    }

}

