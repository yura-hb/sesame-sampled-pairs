import static jdk.nashorn.internal.codegen.ObjectClassGenerator.createSetter;
import static jdk.nashorn.internal.lookup.Lookup.MH;
import static jdk.nashorn.internal.lookup.MethodHandleFactory.stripName;
import static jdk.nashorn.internal.runtime.JSType.getAccessorTypeIndex;
import java.lang.invoke.MethodHandle;
import java.util.logging.Level;
import jdk.nashorn.internal.codegen.ObjectClassGenerator;
import jdk.nashorn.internal.lookup.Lookup;
import jdk.nashorn.internal.objects.Global;

class AccessorProperty extends Property {
    /**
     * Set initial value of a script object's property
     * @param owner        owner
     * @param initialValue initial value
     */
    protected final void setInitialValue(final ScriptObject owner, final Object initialValue) {
	setType(hasDualFields() ? JSType.unboxedFieldType(initialValue) : Object.class);
	if (initialValue instanceof Integer) {
	    invokeSetter(owner, ((Integer) initialValue).intValue());
	} else if (initialValue instanceof Double) {
	    invokeSetter(owner, ((Double) initialValue).doubleValue());
	} else {
	    invokeSetter(owner, initialValue);
	}
    }

    private static final MethodHandle INVALIDATE_SP = findOwnMH_S("invalidateSwitchPoint", Object.class,
	    AccessorProperty.class, Object.class);
    private static final MethodHandle REPLACE_MAP = findOwnMH_S("replaceMap", Object.class, Object.class,
	    PropertyMap.class);
    /** Seed setter for the primitive version of this field (in -Dnashorn.fields.dual=true mode) */
    transient MethodHandle primitiveSetter;
    /** Seed setter for the Object version of this field */
    transient MethodHandle objectSetter;
    /**
     * Property getter cache
     *   Note that we can't do the same simple caching for optimistic getters,
     *   due to the fact that they are bound to a program point, which will
     *   produce different boun method handles wrapping the same access mechanism
     *   depending on callsite
     */
    private transient MethodHandle[] GETTER_CACHE = new MethodHandle[NOOF_TYPES];
    private static final int NOOF_TYPES = getNumberOfAccessorTypes();
    /** Seed getter for the primitive version of this field (in -Dnashorn.fields.dual=true mode) */
    transient MethodHandle primitiveGetter;
    /** Seed getter for the Object version of this field */
    transient MethodHandle objectGetter;

    /**
      * Invoke setter for this property with a value
      * @param self  owner
      * @param value value
      */
    protected final void invokeSetter(final ScriptObject self, final int value) {
	try {
	    getSetter(int.class, self.getMap()).invokeExact((Object) self, value);
	} catch (final Error | RuntimeException e) {
	    throw e;
	} catch (final Throwable e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Invoke setter for this property with a value
     * @param self  owner
     * @param value value
     */
    protected final void invokeSetter(final ScriptObject self, final double value) {
	try {
	    getSetter(double.class, self.getMap()).invokeExact((Object) self, value);
	} catch (final Error | RuntimeException e) {
	    throw e;
	} catch (final Throwable e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Invoke setter for this property with a value
     * @param self  owner
     * @param value value
     */
    protected final void invokeSetter(final ScriptObject self, final Object value) {
	try {
	    getSetter(Object.class, self.getMap()).invokeExact((Object) self, value);
	} catch (final Error | RuntimeException e) {
	    throw e;
	} catch (final Throwable e) {
	    throw new RuntimeException(e);
	}
    }

    @Override
    public MethodHandle getSetter(final Class&lt;?&gt; type, final PropertyMap currentMap) {
	checkUndeclared();

	final int typeIndex = getAccessorTypeIndex(type);
	final int currentTypeIndex = getAccessorTypeIndex(getLocalType());

	//if we are asking for an object setter, but are still a primitive type, we might try to box it
	MethodHandle mh;
	if (needsInvalidator(typeIndex, currentTypeIndex)) {
	    final Property newProperty = getWiderProperty(type);
	    final PropertyMap newMap = getWiderMap(currentMap, newProperty);

	    final MethodHandle widerSetter = newProperty.getSetter(type, newMap);
	    final Class&lt;?&gt; ct = getLocalType();
	    mh = MH.filterArguments(widerSetter, 0,
		    MH.insertArguments(debugReplace(ct, type, currentMap, newMap), 1, newMap));
	    if (ct != null && ct.isPrimitive() && !type.isPrimitive()) {
		mh = ObjectClassGenerator.createGuardBoxedPrimitiveSetter(ct, generateSetter(ct, ct), mh);
	    }
	} else {
	    final Class&lt;?&gt; forType = isUndefined() ? type : getLocalType();
	    mh = generateSetter(!forType.isPrimitive() ? Object.class : forType, type);
	}

	if (isBuiltin()) {
	    mh = MH.filterArguments(mh, 0,
		    debugInvalidate(MH.insertArguments(INVALIDATE_SP, 0, this), getKey().toString()));
	}

	assert mh.type().returnType() == void.class : mh.type();

	return mh;
    }

    private void checkUndeclared() {
	if ((getFlags() & NEEDS_DECLARATION) != 0) {
	    // a lexically defined variable that hasn't seen its declaration - throw ReferenceError
	    throw ECMAErrors.referenceError("not.defined", getKey().toString());
	}
    }

    private boolean needsInvalidator(final int typeIndex, final int currentTypeIndex) {
	return canChangeType() && typeIndex &gt; currentTypeIndex;
    }

    private Property getWiderProperty(final Class&lt;?&gt; type) {
	return copy(type); //invalidate cache of new property
    }

    private PropertyMap getWiderMap(final PropertyMap oldMap, final Property newProperty) {
	final PropertyMap newMap = oldMap.replaceProperty(this, newProperty);
	assert oldMap.size() &gt; 0;
	assert newMap.size() == oldMap.size();
	return newMap;
    }

    private MethodHandle debugReplace(final Class&lt;?&gt; oldType, final Class&lt;?&gt; newType, final PropertyMap oldMap,
	    final PropertyMap newMap) {
	if (!Context.DEBUG || !Global.hasInstance()) {
	    return REPLACE_MAP;
	}

	final Context context = Context.getContextTrusted();
	assert context != null;

	MethodHandle mh = context.addLoggingToHandle(ObjectClassGenerator.class, REPLACE_MAP, new Supplier&lt;String&gt;() {
	    @Override
	    public String get() {
		return "Type change for '" + getKey() + "' " + oldType + "=&gt;" + newType;
	    }
	});

	mh = context.addLoggingToHandle(ObjectClassGenerator.class, Level.FINEST, mh, Integer.MAX_VALUE, false,
		new Supplier&lt;String&gt;() {
		    @Override
		    public String get() {
			return "Setting map " + Debug.id(oldMap) + " =&gt; " + Debug.id(newMap) + " " + oldMap + " =&gt; "
				+ newMap;
		    }
		});
	return mh;
    }

    private MethodHandle generateSetter(final Class&lt;?&gt; forType, final Class&lt;?&gt; type) {
	return debug(createSetter(forType, type, primitiveSetter, objectSetter), getLocalType(), type, "set");
    }

    /**
     * Is this property of the undefined type?
     * @return true if undefined
     */
    protected final boolean isUndefined() {
	return getLocalType() == null;
    }

    private static MethodHandle debugInvalidate(final MethodHandle invalidator, final String key) {
	if (!Context.DEBUG || !Global.hasInstance()) {
	    return invalidator;
	}

	final Context context = Context.getContextTrusted();
	assert context != null;

	return context.addLoggingToHandle(ObjectClassGenerator.class, invalidator, new Supplier&lt;String&gt;() {
	    @Override
	    public String get() {
		return "Field change callback for " + key + " triggered ";
	    }
	});
    }

    @Override
    public final boolean canChangeType() {
	if (!hasDualFields()) {
	    return false;
	}
	// Return true for currently undefined even if non-writable/configurable to allow initialization of ES6 CONST.
	return getLocalType() == null || (getLocalType() != Object.class && (isConfigurable() || isWritable()));
    }

    @Override
    public Property copy(final Class&lt;?&gt; newType) {
	return new AccessorProperty(this, newType);
    }

    private MethodHandle debug(final MethodHandle mh, final Class&lt;?&gt; forType, final Class&lt;?&gt; type, final String tag) {
	if (!Context.DEBUG || !Global.hasInstance()) {
	    return mh;
	}

	final Context context = Context.getContextTrusted();
	assert context != null;

	return context.addLoggingToHandle(ObjectClassGenerator.class, Level.INFO, mh, 0, true, new Supplier&lt;String&gt;() {
	    @Override
	    public String get() {
		return tag + " '" + getKey() + "' (property=" + Debug.id(this) + ", slot=" + getSlot() + " "
			+ getClass().getSimpleName() + " forType=" + stripName(forType) + ", type=" + stripName(type)
			+ ')';
	    }
	});
    }

    /**
     * Copy constructor that may change type and in that case clear the cache. Important to do that before
     * type change or getters will be created already stale.
     *
     * @param property property
     * @param newType  new type
     */
    protected AccessorProperty(final AccessorProperty property, final Class&lt;?&gt; newType) {
	super(property, property.getFlags());

	this.GETTER_CACHE = newType != property.getLocalType() ? new MethodHandle[NOOF_TYPES] : property.GETTER_CACHE;
	this.primitiveGetter = property.primitiveGetter;
	this.primitiveSetter = property.primitiveSetter;
	this.objectGetter = property.objectGetter;
	this.objectSetter = property.objectSetter;

	setType(newType);
    }

}

