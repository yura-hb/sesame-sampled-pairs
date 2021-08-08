import jdk.internal.reflect.FieldAccessor;
import jdk.internal.reflect.Reflection;

class Field extends AccessibleObject implements Member {
    /**
     * Gets the value of a static or instance field of type
     * {@code int} or of another primitive type convertible to
     * type {@code int} via a widening conversion.
     *
     * @param obj the object to extract the {@code int} value
     * from
     * @return the value of the field converted to type {@code int}
     *
     * @exception IllegalAccessException    if this {@code Field} object
     *              is enforcing Java language access control and the underlying
     *              field is inaccessible.
     * @exception IllegalArgumentException  if the specified object is not
     *              an instance of the class or interface declaring the
     *              underlying field (or a subclass or implementor
     *              thereof), or if the field value cannot be
     *              converted to the type {@code int} by a
     *              widening conversion.
     * @exception NullPointerException      if the specified object is null
     *              and the field is an instance field.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     * @see       Field#get
     */
    @CallerSensitive
    @ForceInline // to ensure Reflection.getCallerClass optimization
    public int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException {
	if (!override) {
	    Class&lt;?&gt; caller = Reflection.getCallerClass();
	    checkAccess(caller, obj);
	}
	return getFieldAccessor(obj).getInt(obj);
    }

    private Class&lt;?&gt; clazz;
    private int modifiers;
    private FieldAccessor overrideFieldAccessor;
    private FieldAccessor fieldAccessor;
    private Field root;

    private void checkAccess(Class&lt;?&gt; caller, Object obj) throws IllegalAccessException {
	checkAccess(caller, clazz, Modifier.isStatic(modifiers) ? null : obj.getClass(), modifiers);
    }

    private FieldAccessor getFieldAccessor(Object obj) throws IllegalAccessException {
	boolean ov = override;
	FieldAccessor a = (ov) ? overrideFieldAccessor : fieldAccessor;
	return (a != null) ? a : acquireFieldAccessor(ov);
    }

    private FieldAccessor acquireFieldAccessor(boolean overrideFinalCheck) {
	// First check to see if one has been created yet, and take it
	// if so
	FieldAccessor tmp = null;
	if (root != null)
	    tmp = root.getFieldAccessor(overrideFinalCheck);
	if (tmp != null) {
	    if (overrideFinalCheck)
		overrideFieldAccessor = tmp;
	    else
		fieldAccessor = tmp;
	} else {
	    // Otherwise fabricate one and propagate it up to the root
	    tmp = reflectionFactory.newFieldAccessor(this, overrideFinalCheck);
	    setFieldAccessor(tmp, overrideFinalCheck);
	}

	return tmp;
    }

    private FieldAccessor getFieldAccessor(boolean overrideFinalCheck) {
	return (overrideFinalCheck) ? overrideFieldAccessor : fieldAccessor;
    }

    private void setFieldAccessor(FieldAccessor accessor, boolean overrideFinalCheck) {
	if (overrideFinalCheck)
	    overrideFieldAccessor = accessor;
	else
	    fieldAccessor = accessor;
	// Propagate up
	if (root != null) {
	    root.setFieldAccessor(accessor, overrideFinalCheck);
	}
    }

}

