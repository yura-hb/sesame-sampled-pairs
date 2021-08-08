class JSType extends Enum&lt;JSType&gt; {
    /**
     * JavaScript compliant conversion of Object to boolean
     * See ECMA 9.2 ToBoolean
     *
     * @param obj an object
     *
     * @return a boolean
     */
    public static boolean toBoolean(final Object obj) {
	if (obj instanceof Boolean) {
	    return (Boolean) obj;
	}

	if (nullOrUndefined(obj)) {
	    return false;
	}

	if (obj instanceof Number) {
	    final double num = ((Number) obj).doubleValue();
	    return num != 0 && !Double.isNaN(num);
	}

	if (isString(obj)) {
	    return ((CharSequence) obj).length() &gt; 0;
	}

	return true;
    }

    /**
     * Check if an object is null or undefined
     *
     * @param obj object to check
     *
     * @return true if null or undefined
     */
    public static boolean nullOrUndefined(final Object obj) {
	return obj == null || obj == ScriptRuntime.UNDEFINED;
    }

    /**
     * Returns true if object represents a primitive JavaScript string value.
     * @param obj the object
     * @return true if the object represents a primitive JavaScript string value.
     */
    public static boolean isString(final Object obj) {
	return obj instanceof String || obj instanceof ConsString;
    }

}

