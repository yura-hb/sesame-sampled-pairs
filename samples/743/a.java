import org.apache.commons.math4.exception.MathIllegalArgumentException;
import org.apache.commons.math4.exception.util.LocalizedFormats;
import org.apache.commons.math4.util.FastMath;

class FastCosineTransformer implements RealTransformer, Serializable {
    /**
     * Perform the FCT algorithm (including inverse).
     *
     * @param f the real data array to be transformed
     * @return the real transformed array
     * @throws MathIllegalArgumentException if the length of the data array is
     * not a power of two plus one
     */
    protected double[] fct(double[] f) throws MathIllegalArgumentException {

	final double[] transformed = new double[f.length];

	final int n = f.length - 1;
	if (!ArithmeticUtils.isPowerOfTwo(n)) {
	    throw new MathIllegalArgumentException(LocalizedFormats.NOT_POWER_OF_TWO_PLUS_ONE,
		    Integer.valueOf(f.length));
	}
	if (n == 1) { // trivial case
	    transformed[0] = 0.5 * (f[0] + f[1]);
	    transformed[1] = 0.5 * (f[0] - f[1]);
	    return transformed;
	}

	// construct a new array and perform FFT on it
	final double[] x = new double[n];
	x[0] = 0.5 * (f[0] + f[n]);
	x[n &gt;&gt; 1] = f[n &gt;&gt; 1];
	// temporary variable for transformed[1]
	double t1 = 0.5 * (f[0] - f[n]);
	for (int i = 1; i &lt; (n &gt;&gt; 1); i++) {
	    final double a = 0.5 * (f[i] + f[n - i]);
	    final double b = FastMath.sin(i * FastMath.PI / n) * (f[i] - f[n - i]);
	    final double c = FastMath.cos(i * FastMath.PI / n) * (f[i] - f[n - i]);
	    x[i] = a - b;
	    x[n - i] = a + b;
	    t1 += c;
	}
	FastFourierTransformer transformer;
	transformer = new FastFourierTransformer(DftNormalization.STANDARD);
	Complex[] y = transformer.transform(x, TransformType.FORWARD);

	// reconstruct the FCT result for the original array
	transformed[0] = y[0].getReal();
	transformed[1] = t1;
	for (int i = 1; i &lt; (n &gt;&gt; 1); i++) {
	    transformed[2 * i] = y[i].getReal();
	    transformed[2 * i + 1] = transformed[2 * i - 1] - y[i].getImaginary();
	}
	transformed[n] = y[n &gt;&gt; 1].getReal();

	return transformed;
    }

}

