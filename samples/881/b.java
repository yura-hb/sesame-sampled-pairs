class ScriptRuntime {
    /**
     * Generic implementation of ECMA 9.12 - SameValue algorithm
     *
     * @param x first value to compare
     * @param y second value to compare
     *
     * @return true if both objects have the same value
     */
    public static boolean sameValue(final Object x, final Object y) {
	final JSType xType = JSType.ofNoFunction(x);
	final JSType yType = JSType.ofNoFunction(y);

	if (xType != yType) {
	    return false;
	}

	if (xType == JSType.UNDEFINED || xType == JSType.NULL) {
	    return true;
	}

	if (xType == JSType.NUMBER) {
	    final double xVal = ((Number) x).doubleValue();
	    final double yVal = ((Number) y).doubleValue();

	    if (Double.isNaN(xVal) && Double.isNaN(yVal)) {
		return true;
	    }

	    // checking for xVal == -0.0 and yVal == +0.0 or vice versa
	    if (xVal == 0.0 && Double.doubleToLongBits(xVal) != Double.doubleToLongBits(yVal)) {
		return false;
	    }

	    return xVal == yVal;
	}

	if (xType == JSType.STRING || yType == JSType.BOOLEAN) {
	    return x.equals(y);
	}

	return x == y;
    }

}

