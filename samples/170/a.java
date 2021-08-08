import org.apache.commons.lang3.Validate;

class IEEE754rUtils {
    /**
     * &lt;p&gt;Returns the minimum value in an array.&lt;/p&gt;
     *
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if &lt;code&gt;array&lt;/code&gt; is &lt;code&gt;null&lt;/code&gt;
     * @throws IllegalArgumentException if &lt;code&gt;array&lt;/code&gt; is empty
      * @since 3.4 Changed signature from min(double[]) to min(double...)
     */
    public static double min(final double... array) {
	Validate.isTrue(array != null, "The Array must not be null");
	Validate.isTrue(array.length != 0, "Array cannot be empty.");

	// Finds and returns min
	double min = array[0];
	for (int i = 1; i &lt; array.length; i++) {
	    min = min(array[i], min);
	}

	return min;
    }

    /**
     * &lt;p&gt;Gets the minimum of two &lt;code&gt;double&lt;/code&gt; values.&lt;/p&gt;
     *
     * &lt;p&gt;NaN is only returned if all numbers are NaN as per IEEE-754r. &lt;/p&gt;
     *
     * @param a  value 1
     * @param b  value 2
     * @return  the smallest of the values
     */
    public static double min(final double a, final double b) {
	if (Double.isNaN(a)) {
	    return b;
	} else if (Double.isNaN(b)) {
	    return a;
	} else {
	    return Math.min(a, b);
	}
    }

}

