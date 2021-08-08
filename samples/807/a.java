import org.apache.commons.math4.util.FastMath;

class QRDecomposition {
    /** Decompose matrix.
     * @param matrix transposed matrix
     * @since 3.2
     */
    protected void decompose(double[][] matrix) {
	for (int minor = 0; minor &lt; FastMath.min(matrix.length, matrix[0].length); minor++) {
	    performHouseholderReflection(minor, matrix);
	}
    }

    /** The diagonal elements of R. */
    private double[] rDiag;

    /** Perform Householder reflection for a minor A(minor, minor) of A.
     * @param minor minor index
     * @param matrix transposed matrix
     * @since 3.2
     */
    protected void performHouseholderReflection(int minor, double[][] matrix) {

	final double[] qrtMinor = matrix[minor];

	/*
	 * Let x be the first column of the minor, and a^2 = |x|^2.
	 * x will be in the positions qr[minor][minor] through qr[m][minor].
	 * The first column of the transformed minor will be (a,0,0,..)'
	 * The sign of a is chosen to be opposite to the sign of the first
	 * component of x. Let's find a:
	 */
	double xNormSqr = 0;
	for (int row = minor; row &lt; qrtMinor.length; row++) {
	    final double c = qrtMinor[row];
	    xNormSqr += c * c;
	}
	final double a = (qrtMinor[minor] &gt; 0) ? -FastMath.sqrt(xNormSqr) : FastMath.sqrt(xNormSqr);
	rDiag[minor] = a;

	if (a != 0.0) {

	    /*
	     * Calculate the normalized reflection vector v and transform
	     * the first column. We know the norm of v beforehand: v = x-ae
	     * so |v|^2 = &lt;x-ae,x-ae&gt; = &lt;x,x&gt;-2a&lt;x,e&gt;+a^2&lt;e,e&gt; =
	     * a^2+a^2-2a&lt;x,e&gt; = 2a*(a - &lt;x,e&gt;).
	     * Here &lt;x, e&gt; is now qr[minor][minor].
	     * v = x-ae is stored in the column at qr:
	     */
	    qrtMinor[minor] -= a; // now |v|^2 = -2a*(qr[minor][minor])

	    /*
	     * Transform the rest of the columns of the minor:
	     * They will be transformed by the matrix H = I-2vv'/|v|^2.
	     * If x is a column vector of the minor, then
	     * Hx = (I-2vv'/|v|^2)x = x-2vv'x/|v|^2 = x - 2&lt;x,v&gt;/|v|^2 v.
	     * Therefore the transformation is easily calculated by
	     * subtracting the column vector (2&lt;x,v&gt;/|v|^2)v from x.
	     *
	     * Let 2&lt;x,v&gt;/|v|^2 = alpha. From above we have
	     * |v|^2 = -2a*(qr[minor][minor]), so
	     * alpha = -&lt;x,v&gt;/(a*qr[minor][minor])
	     */
	    for (int col = minor + 1; col &lt; matrix.length; col++) {
		final double[] qrtCol = matrix[col];
		double alpha = 0;
		for (int row = minor; row &lt; qrtCol.length; row++) {
		    alpha -= qrtCol[row] * qrtMinor[row];
		}
		alpha /= a * qrtMinor[minor];

		// Subtract the column vector alpha*v from x.
		for (int row = minor; row &lt; qrtCol.length; row++) {
		    qrtCol[row] -= alpha * qrtMinor[row];
		}
	    }
	}
    }

}

