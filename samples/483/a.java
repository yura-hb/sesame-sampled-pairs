import org.apache.commons.math4.exception.NoDataException;
import org.apache.commons.math4.exception.util.LocalizedFormats;
import org.apache.commons.math4.util.MathUtils;

class PolynomialFunction implements UnivariateDifferentiableFunction, Serializable {
    /**
     * Negate the instance.
     *
     * @return a new polynomial with all coefficients negated
     */
    public PolynomialFunction negate() {
	double[] newCoefficients = new double[coefficients.length];
	for (int i = 0; i &lt; coefficients.length; ++i) {
	    newCoefficients[i] = -coefficients[i];
	}
	return new PolynomialFunction(newCoefficients);
    }

    /**
     * The coefficients of the polynomial, ordered by degree -- i.e.,
     * coefficients[0] is the constant term and coefficients[n] is the
     * coefficient of x^n where n is the degree of the polynomial.
     */
    private final double coefficients[];

    /**
     * Construct a polynomial with the given coefficients.  The first element
     * of the coefficients array is the constant term.  Higher degree
     * coefficients follow in sequence.  The degree of the resulting polynomial
     * is the index of the last non-null element of the array, or 0 if all elements
     * are null.
     * &lt;p&gt;
     * The constructor makes a copy of the input array and assigns the copy to
     * the coefficients property.&lt;/p&gt;
     *
     * @param c Polynomial coefficients.
     * @throws NullArgumentException if {@code c} is {@code null}.
     * @throws NoDataException if {@code c} is empty.
     */
    public PolynomialFunction(double c[]) throws NullArgumentException, NoDataException {
	super();
	MathUtils.checkNotNull(c);
	int n = c.length;
	if (n == 0) {
	    throw new NoDataException(LocalizedFormats.EMPTY_POLYNOMIALS_COEFFICIENTS_ARRAY);
	}
	while ((n &gt; 1) && (c[n - 1] == 0)) {
	    --n;
	}
	this.coefficients = new double[n];
	System.arraycopy(c, 0, this.coefficients, 0, n);
    }

}

