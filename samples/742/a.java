class MathArrays {
    /**
     * &lt;p&gt;Multiply each element of an array by a value.&lt;/p&gt;
     *
     * &lt;p&gt;The array is modified in place (no copy is created).&lt;/p&gt;
     *
     * @param arr Array to scale
     * @param val Scalar
     * @since 3.2
     */
    public static void scaleInPlace(double val, final double[] arr) {
	for (int i = 0; i &lt; arr.length; i++) {
	    arr[i] *= val;
	}
    }

}

