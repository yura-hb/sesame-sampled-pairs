class FastMath {
    /**
     * Returns the first argument with the sign of the second argument.
     * A NaN {@code sign} argument is treated as positive.
     *
     * @param magnitude the value to return
     * @param sign the sign for the returned value
     * @return the magnitude with the same sign as the {@code sign} argument
     */
    public static double copySign(double magnitude, double sign) {
	// The highest order bit is going to be zero if the
	// highest order bit of m and s is the same and one otherwise.
	// So (m^s) will be positive if both m and s have the same sign
	// and negative otherwise.
	final long m = Double.doubleToRawLongBits(magnitude); // don't care about NaN
	final long s = Double.doubleToRawLongBits(sign);
	if ((m ^ s) &gt;= 0) {
	    return magnitude;
	}
	return -magnitude; // flip sign
    }

}

