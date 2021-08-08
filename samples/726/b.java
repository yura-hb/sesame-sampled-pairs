import static com.sun.tools.javac.code.Symbol.*;
import static com.sun.tools.javac.code.Type.*;

class Types {
    /**
     * Return the primitive type corresponding to a boxed type.
     */
    public Type unboxedType(Type t) {
	for (int i = 0; i &lt; syms.boxedName.length; i++) {
	    Name box = syms.boxedName[i];
	    if (box != null && asSuper(t, syms.enterClass(syms.java_base, box)) != null)
		return syms.typeOfTag[i];
	}
	return Type.noType;
    }

    final Symtab syms;
    private SimpleVisitor&lt;Type, Symbol&gt; asSuper = new SimpleVisitor&lt;Type, Symbol&gt;() {

	public Type visitType(Type t, Symbol sym) {
	    return null;
	}

	@Override
	public Type visitClassType(ClassType t, Symbol sym) {
	    if (t.tsym == sym)
		return t;

	    Type st = supertype(t);
	    if (st.hasTag(CLASS) || st.hasTag(TYPEVAR)) {
		Type x = asSuper(st, sym);
		if (x != null)
		    return x;
	    }
	    if ((sym.flags() & INTERFACE) != 0) {
		for (List&lt;Type&gt; l = interfaces(t); l.nonEmpty(); l = l.tail) {
		    if (!l.head.hasTag(ERROR)) {
			Type x = asSuper(l.head, sym);
			if (x != null)
			    return x;
		    }
		}
	    }
	    return null;
	}

	@Override
	public Type visitArrayType(ArrayType t, Symbol sym) {
	    return isSubtype(t, sym.type) ? sym.type : null;
	}

	@Override
	public Type visitTypeVar(TypeVar t, Symbol sym) {
	    if (t.tsym == sym)
		return t;
	    else
		return asSuper(t.bound, sym);
	}

	@Override
	public Type visitErrorType(ErrorType t, Symbol sym) {
	    return t;
	}
    };

    /**
     * Return the (most specific) base type of t that starts with the
     * given symbol.  If none exists, return null.
     *
     * Caveat Emptor: Since javac represents the class of all arrays with a singleton
     * symbol Symtab.arrayClass, which by being a singleton cannot hold any discriminant,
     * this method could yield surprising answers when invoked on arrays. For example when
     * invoked with t being byte [] and sym being t.sym itself, asSuper would answer null.
     *
     * @param t a type
     * @param sym a symbol
     */
    public Type asSuper(Type t, Symbol sym) {
	/* Some examples:
	 *
	 * (Enum&lt;E&gt;, Comparable) =&gt; Comparable&lt;E&gt;
	 * (c.s.s.d.AttributeTree.ValueKind, Enum) =&gt; Enum&lt;c.s.s.d.AttributeTree.ValueKind&gt;
	 * (c.s.s.t.ExpressionTree, c.s.s.t.Tree) =&gt; c.s.s.t.Tree
	 * (j.u.List&lt;capture#160 of ? extends c.s.s.d.DocTree&gt;, Iterable) =&gt;
	 *     Iterable&lt;capture#160 of ? extends c.s.s.d.DocTree&gt;
	 */
	if (sym.type == syms.objectType) { //optimization
	    return syms.objectType;
	}
	return asSuper.visit(t, sym);
    }

    abstract class DefaultTypeVisitor&lt;R, S&gt; implements Visitor&lt;R, S&gt; {
	final Symtab syms;
	private SimpleVisitor&lt;Type, Symbol&gt; asSuper = new SimpleVisitor&lt;Type, Symbol&gt;() {

	    public Type visitType(Type t, Symbol sym) {
		return null;
	    }

	    @Override
	    public Type visitClassType(ClassType t, Symbol sym) {
		if (t.tsym == sym)
		    return t;

		Type st = supertype(t);
		if (st.hasTag(CLASS) || st.hasTag(TYPEVAR)) {
		    Type x = asSuper(st, sym);
		    if (x != null)
			return x;
		}
		if ((sym.flags() & INTERFACE) != 0) {
		    for (List&lt;Type&gt; l = interfaces(t); l.nonEmpty(); l = l.tail) {
			if (!l.head.hasTag(ERROR)) {
			    Type x = asSuper(l.head, sym);
			    if (x != null)
				return x;
			}
		    }
		}
		return null;
	    }

	    @Override
	    public Type visitArrayType(ArrayType t, Symbol sym) {
		return isSubtype(t, sym.type) ? sym.type : null;
	    }

	    @Override
	    public Type visitTypeVar(TypeVar t, Symbol sym) {
		if (t.tsym == sym)
		    return t;
		else
		    return asSuper(t.bound, sym);
	    }

	    @Override
	    public Type visitErrorType(ErrorType t, Symbol sym) {
		return t;
	    }
	};

	final public R visit(Type t, S s) {
	    return t.accept(this, s);
	}

    }

}

