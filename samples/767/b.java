import java.lang.ref.WeakReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import static java.lang.invoke.MethodHandleStatics.*;

class MethodType implements Serializable {
    /** Delete the last parameter type and replace it with arrayLength copies of the component type of arrayType.
     * @param arrayType any array type
     * @param pos position at which to insert parameters
     * @param arrayLength the number of parameter types to insert
     * @return the resulting type
     */
    /*non-public*/ MethodType asCollectorType(Class&lt;?&gt; arrayType, int pos, int arrayLength) {
	assert (parameterCount() &gt;= 1);
	assert (pos &lt; ptypes.length);
	assert (ptypes[pos].isAssignableFrom(arrayType));
	MethodType res;
	if (arrayType == Object[].class) {
	    res = genericMethodType(arrayLength);
	    if (rtype != Object.class) {
		res = res.changeReturnType(rtype);
	    }
	} else {
	    Class&lt;?&gt; elemType = arrayType.getComponentType();
	    assert (elemType != null);
	    res = methodType(rtype, Collections.nCopies(arrayLength, elemType));
	}
	if (ptypes.length == 1) {
	    return res;
	} else {
	    // insert after (if need be), then before
	    if (pos &lt; ptypes.length - 1) {
		res = res.insertParameterTypes(arrayLength, Arrays.copyOfRange(ptypes, pos + 1, ptypes.length));
	    }
	    return res.insertParameterTypes(0, Arrays.copyOf(ptypes, pos));
	}
    }

    private final @Stable Class&lt;?&gt;[] ptypes;
    private final @Stable Class&lt;?&gt; rtype;
    private static final @Stable MethodType[] objectOnlyTypes = new MethodType[20];
    static final Class&lt;?&gt;[] NO_PTYPES = {};
    static final ConcurrentWeakInternSet&lt;MethodType&gt; internTable = new ConcurrentWeakInternSet&lt;&gt;();
    private @Stable MethodTypeForm form;
    /** This number, mandated by the JVM spec as 255,
     *  is the maximum number of &lt;em&gt;slots&lt;/em&gt;
     *  that any Java method can receive in its argument list.
     *  It limits both JVM signatures and method type objects.
     *  The longest possible invocation will look like
     *  {@code staticMethod(arg1, arg2, ..., arg255)} or
     *  {@code x.virtualMethod(arg1, arg2, ..., arg254)}.
     */
    /*non-public*/ static final int MAX_JVM_ARITY = 255;

    /**
     * Returns the number of parameter types in this method type.
     * @return the number of parameter types
     */
    public int parameterCount() {
	return ptypes.length;
    }

    /**
     * Finds or creates a method type whose components are all {@code Object}.
     * Convenience method for {@link #methodType(java.lang.Class, java.lang.Class[]) methodType}.
     * All parameters and the return type will be Object.
     * @param objectArgCount number of parameters
     * @return a generally applicable method type, for all calls of the given argument count
     * @throws IllegalArgumentException if {@code objectArgCount} is negative or greater than 255
     * @see #genericMethodType(int, boolean)
     */
    public static MethodType genericMethodType(int objectArgCount) {
	return genericMethodType(objectArgCount, false);
    }

    /**
     * Finds or creates a method type with a different return type.
     * Convenience method for {@link #methodType(java.lang.Class, java.lang.Class[]) methodType}.
     * @param nrtype a return parameter type to replace the old one with
     * @return the same type, except with the return type change
     * @throws NullPointerException if {@code nrtype} is null
     */
    public MethodType changeReturnType(Class&lt;?&gt; nrtype) {
	if (returnType() == nrtype)
	    return this;
	return makeImpl(nrtype, ptypes, true);
    }

    /**
     * Finds or creates a method type with the given components.
     * Convenience method for {@link #methodType(java.lang.Class, java.lang.Class[]) methodType}.
     * @param rtype  the return type
     * @param ptypes the parameter types
     * @return a method type with the given components
     * @throws NullPointerException if {@code rtype} or {@code ptypes} or any element of {@code ptypes} is null
     * @throws IllegalArgumentException if any element of {@code ptypes} is {@code void.class}
     */
    public static MethodType methodType(Class&lt;?&gt; rtype, List&lt;Class&lt;?&gt;&gt; ptypes) {
	boolean notrust = false; // random List impl. could return evil ptypes array
	return makeImpl(rtype, listToArray(ptypes), notrust);
    }

    /**
     * Finds or creates a method type with additional parameter types.
     * Convenience method for {@link #methodType(java.lang.Class, java.lang.Class[]) methodType}.
     * @param num    the position (zero-based) of the inserted parameter type(s)
     * @param ptypesToInsert zero or more new parameter types to insert into the parameter list
     * @return the same type, except with the selected parameter(s) inserted
     * @throws IndexOutOfBoundsException if {@code num} is negative or greater than {@code parameterCount()}
     * @throws IllegalArgumentException if any element of {@code ptypesToInsert} is {@code void.class}
     *                                  or if the resulting method type would have more than 255 parameter slots
     * @throws NullPointerException if {@code ptypesToInsert} or any of its elements is null
     */
    public MethodType insertParameterTypes(int num, Class&lt;?&gt;... ptypesToInsert) {
	int len = ptypes.length;
	if (num &lt; 0 || num &gt; len)
	    throw newIndexOutOfBoundsException(num);
	int ins = checkPtypes(ptypesToInsert);
	checkSlotCount(parameterSlotCount() + ptypesToInsert.length + ins);
	int ilen = ptypesToInsert.length;
	if (ilen == 0)
	    return this;
	Class&lt;?&gt;[] nptypes = new Class&lt;?&gt;[len + ilen];
	if (num &gt; 0) {
	    System.arraycopy(ptypes, 0, nptypes, 0, num);
	}
	System.arraycopy(ptypesToInsert, 0, nptypes, num, ilen);
	if (num &lt; len) {
	    System.arraycopy(ptypes, num, nptypes, num + ilen, len - num);
	}
	return makeImpl(rtype, nptypes, true);
    }

    /**
     * Finds or creates a method type whose components are {@code Object} with an optional trailing {@code Object[]} array.
     * Convenience method for {@link #methodType(java.lang.Class, java.lang.Class[]) methodType}.
     * All parameters and the return type will be {@code Object},
     * except the final array parameter if any, which will be {@code Object[]}.
     * @param objectArgCount number of parameters (excluding the final array parameter if any)
     * @param finalArray whether there will be a trailing array parameter, of type {@code Object[]}
     * @return a generally applicable method type, for all calls of the given fixed argument count and a collected array of further arguments
     * @throws IllegalArgumentException if {@code objectArgCount} is negative or greater than 255 (or 254, if {@code finalArray} is true)
     * @see #genericMethodType(int)
     */
    public static MethodType genericMethodType(int objectArgCount, boolean finalArray) {
	MethodType mt;
	checkSlotCount(objectArgCount);
	int ivarargs = (!finalArray ? 0 : 1);
	int ootIndex = objectArgCount * 2 + ivarargs;
	if (ootIndex &lt; objectOnlyTypes.length) {
	    mt = objectOnlyTypes[ootIndex];
	    if (mt != null)
		return mt;
	}
	Class&lt;?&gt;[] ptypes = new Class&lt;?&gt;[objectArgCount + ivarargs];
	Arrays.fill(ptypes, Object.class);
	if (ivarargs != 0)
	    ptypes[objectArgCount] = Object[].class;
	mt = makeImpl(Object.class, ptypes, true);
	if (ootIndex &lt; objectOnlyTypes.length) {
	    objectOnlyTypes[ootIndex] = mt; // cache it here also!
	}
	return mt;
    }

    /**
     * Returns the return type of this method type.
     * @return the return type
     */
    public Class&lt;?&gt; returnType() {
	return rtype;
    }

    /**
     * Sole factory method to find or create an interned method type.
     * @param rtype desired return type
     * @param ptypes desired parameter types
     * @param trusted whether the ptypes can be used without cloning
     * @return the unique method type of the desired structure
     */
    /*trusted*/ static MethodType makeImpl(Class&lt;?&gt; rtype, Class&lt;?&gt;[] ptypes, boolean trusted) {
	if (ptypes.length == 0) {
	    ptypes = NO_PTYPES;
	    trusted = true;
	}
	MethodType primordialMT = new MethodType(rtype, ptypes);
	MethodType mt = internTable.get(primordialMT);
	if (mt != null)
	    return mt;

	// promote the object to the Real Thing, and reprobe
	MethodType.checkRtype(rtype);
	if (trusted) {
	    MethodType.checkPtypes(ptypes);
	    mt = primordialMT;
	} else {
	    // Make defensive copy then validate
	    ptypes = Arrays.copyOf(ptypes, ptypes.length);
	    MethodType.checkPtypes(ptypes);
	    mt = new MethodType(rtype, ptypes);
	}
	mt.form = MethodTypeForm.findForm(mt);
	return internTable.add(mt);
    }

    private static Class&lt;?&gt;[] listToArray(List&lt;Class&lt;?&gt;&gt; ptypes) {
	// sanity check the size before the toArray call, since size might be huge
	checkSlotCount(ptypes.size());
	return ptypes.toArray(NO_PTYPES);
    }

    private static IndexOutOfBoundsException newIndexOutOfBoundsException(Object num) {
	if (num instanceof Integer)
	    num = "bad index: " + num;
	return new IndexOutOfBoundsException(num.toString());
    }

    /** Return number of extra slots (count of long/double args). */
    private static int checkPtypes(Class&lt;?&gt;[] ptypes) {
	int slots = 0;
	for (Class&lt;?&gt; ptype : ptypes) {
	    checkPtype(ptype);
	    if (ptype == double.class || ptype == long.class) {
		slots++;
	    }
	}
	checkSlotCount(ptypes.length + slots);
	return slots;
    }

    /** Reports the number of JVM stack slots required to invoke a method
     * of this type.  Note that (for historical reasons) the JVM requires
     * a second stack slot to pass long and double arguments.
     * So this method returns {@link #parameterCount() parameterCount} plus the
     * number of long and double parameters (if any).
     * &lt;p&gt;
     * This method is included for the benefit of applications that must
     * generate bytecodes that process method handles and invokedynamic.
     * @return the number of JVM stack slots for this type's parameters
     */
    /*non-public*/ int parameterSlotCount() {
	return form.parameterSlotCount();
    }

    static void checkSlotCount(int count) {
	if ((count & MAX_JVM_ARITY) != count)
	    throw newIllegalArgumentException("bad parameter count " + count);
    }

    /**
     * Constructor that performs no copying or validation.
     * Should only be called from the factory method makeImpl
     */
    private MethodType(Class&lt;?&gt; rtype, Class&lt;?&gt;[] ptypes) {
	this.rtype = rtype;
	this.ptypes = ptypes;
    }

    private static void checkRtype(Class&lt;?&gt; rtype) {
	Objects.requireNonNull(rtype);
    }

    private static void checkPtype(Class&lt;?&gt; ptype) {
	Objects.requireNonNull(ptype);
	if (ptype == void.class)
	    throw newIllegalArgumentException("parameter type cannot be void");
    }

    class ConcurrentWeakInternSet&lt;T&gt; {
	private final @Stable Class&lt;?&gt;[] ptypes;
	private final @Stable Class&lt;?&gt; rtype;
	private static final @Stable MethodType[] objectOnlyTypes = new MethodType[20];
	static final Class&lt;?&gt;[] NO_PTYPES = {};
	static final ConcurrentWeakInternSet&lt;MethodType&gt; internTable = new ConcurrentWeakInternSet&lt;&gt;();
	private @Stable MethodTypeForm form;
	/** This number, mandated by the JVM spec as 255,
	*  is the maximum number of &lt;em&gt;slots&lt;/em&gt;
	*  that any Java method can receive in its argument list.
	*  It limits both JVM signatures and method type objects.
	*  The longest possible invocation will look like
	*  {@code staticMethod(arg1, arg2, ..., arg255)} or
	*  {@code x.virtualMethod(arg1, arg2, ..., arg254)}.
	*/
	/*non-public*/ static final int MAX_JVM_ARITY = 255;

	/**
	 * Get the existing interned element.
	 * This method returns null if no element is interned.
	 *
	 * @param elem element to look up
	 * @return the interned element
	 */
	public T get(T elem) {
	    if (elem == null)
		throw new NullPointerException();
	    expungeStaleElements();

	    WeakEntry&lt;T&gt; value = map.get(new WeakEntry&lt;&gt;(elem));
	    if (value != null) {
		T res = value.get();
		if (res != null) {
		    return res;
		}
	    }
	    return null;
	}

	/**
	 * Interns the element.
	 * Always returns non-null element, matching the one in the intern set.
	 * Under the race against another add(), it can return &lt;i&gt;different&lt;/i&gt;
	 * element, if another thread beats us to interning it.
	 *
	 * @param elem element to add
	 * @return element that was actually added
	 */
	public T add(T elem) {
	    if (elem == null)
		throw new NullPointerException();

	    // Playing double race here, and so spinloop is required.
	    // First race is with two concurrent updaters.
	    // Second race is with GC purging weak ref under our feet.
	    // Hopefully, we almost always end up with a single pass.
	    T interned;
	    WeakEntry&lt;T&gt; e = new WeakEntry&lt;&gt;(elem, stale);
	    do {
		expungeStaleElements();
		WeakEntry&lt;T&gt; exist = map.putIfAbsent(e, e);
		interned = (exist == null) ? elem : exist.get();
	    } while (interned == null);
	    return interned;
	}

	private void expungeStaleElements() {
	    Reference&lt;? extends T&gt; reference;
	    while ((reference = stale.poll()) != null) {
		map.remove(reference);
	    }
	}

	class WeakEntry&lt;T&gt; extends WeakReference&lt;T&gt; {
	    private final ConcurrentMap&lt;WeakEntry&lt;T&gt;, WeakEntry&lt;T&gt;&gt; map;
	    private final ReferenceQueue&lt;T&gt; stale;

	    public WeakEntry(T key) {
		super(key);
		hashcode = key.hashCode();
	    }

	    public WeakEntry(T key, ReferenceQueue&lt;T&gt; queue) {
		super(key, queue);
		hashcode = key.hashCode();
	    }

	}

    }

}

