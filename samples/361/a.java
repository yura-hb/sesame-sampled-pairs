import static com.google.common.primitives.Doubles.isFinite;
import static java.lang.Double.NaN;

class StatsAccumulator {
    /** Adds the given value to the dataset. */
    public void add(double value) {
	if (count == 0) {
	    count = 1;
	    mean = value;
	    min = value;
	    max = value;
	    if (!isFinite(value)) {
		sumOfSquaresOfDeltas = NaN;
	    }
	} else {
	    count++;
	    if (isFinite(value) && isFinite(mean)) {
		// Art of Computer Programming vol. 2, Knuth, 4.2.2, (15) and (16)
		double delta = value - mean;
		mean += delta / count;
		sumOfSquaresOfDeltas += delta * (value - mean);
	    } else {
		mean = calculateNewMeanNonFinite(mean, value);
		sumOfSquaresOfDeltas = NaN;
	    }
	    min = Math.min(min, value);
	    max = Math.max(max, value);
	}
    }

    private long count = 0;
    private double mean = 0.0;
    private double min = NaN;
    private double max = NaN;
    private double sumOfSquaresOfDeltas = 0.0;

    /**
    * Calculates the new value for the accumulated mean when a value is added, in the case where at
    * least one of the previous mean and the value is non-finite.
    */
    static double calculateNewMeanNonFinite(double previousMean, double value) {
	/*
	 * Desired behaviour is to match the results of applying the naive mean formula. In particular,
	 * the update formula can subtract infinities in cases where the naive formula would add them.
	 *
	 * Consequently:
	 * 1. If the previous mean is finite and the new value is non-finite then the new mean is that
	 *    value (whether it is NaN or infinity).
	 * 2. If the new value is finite and the previous mean is non-finite then the mean is unchanged
	 *    (whether it is NaN or infinity).
	 * 3. If both the previous mean and the new value are non-finite and...
	 * 3a. ...either or both is NaN (so mean != value) then the new mean is NaN.
	 * 3b. ...they are both the same infinities (so mean == value) then the mean is unchanged.
	 * 3c. ...they are different infinities (so mean != value) then the new mean is NaN.
	 */
	if (isFinite(previousMean)) {
	    // This is case 1.
	    return value;
	} else if (isFinite(value) || previousMean == value) {
	    // This is case 2. or 3b.
	    return previousMean;
	} else {
	    // This is case 3a. or 3c.
	    return NaN;
	}
    }

}

