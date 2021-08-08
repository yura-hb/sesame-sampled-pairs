class MathUtils {
    /**
     * Rounds a double to the given number of decimal places.
     *
     * @param value the double value
     * @param afterDecimalPoint the number of digits after the decimal point
     * @return the double rounded to the given precision
     */
    public static /*@pure@*/ double roundDouble(double value, int afterDecimalPoint) {

	double mask = Math.pow(10.0, (double) afterDecimalPoint);

	return (double) (Math.round(value * mask)) / mask;
    }

}

